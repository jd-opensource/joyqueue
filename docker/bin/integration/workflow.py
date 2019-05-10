# !/usr/bin/env python3
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


import subprocess
from pathlib import Path
import os
import datetime
import logging
import time
import json
from shlex import split
import random
from model.task import Task
from model.workspace import Workspace

logging.basicConfig()
FAILED_TO_LOCK_LOCAL_WORKSPACE = 501
FAILED_TO_PREPARE_PRESSURE = 502
FAILED_TO_PREPARE_MQ_DOCKER = 503
FAILED_TO_INVOKE_MQ_DOCKER = 504
ONLINE_MODE = 'online'


class Workflow:

    def __init__(self, config):
        self.config = config
        self.score_dir = config.get_argv().score_file
        self.benchmark_config = config.get_argv().benchmark_config
        self.task = Task(config)
        self.workspace = Workspace(config)
        self.ssh_user = config.get_value('User', 'SSH')
        self.ssh_passwd_file = config.get_value('PasswdFile', 'SSH')
        self.ssh_port = config.get_value('Port', 'SSH')
        self.mode = config.get_value('Mode')
        self.pwd = os.path.dirname(__file__)
        self.mq_tag = str(time.time()).replace('.', '_')
        self.running_mq_containers = {}
        self.logger = logging.getLogger(__name__)
        self.logger.setLevel(logging.DEBUG)

    def run(self):
        start = time.time()
        try:
            # self.__lock_local_workspace()
            self.__prepare_pressure_worker()
            self.__prepare_mq_worker()
            self.__start_mq_cluster_workers()
            self.__check_mq_cluster_state()
            self.__start_pressure_worker()
            self.__collect_data_from_pressure_worker()
            # self.__mock_crash_recovery()
            result = self.__collect_data()
            self.logger.info('Workflow successful!')
        except WorkflowError as err:
            self.logger.error('Failed to execute workflow.')
        finally:
            self.__cleanup()
            self.logger.info('clean up done!')
        end = time.time()
        self.__save_result(result)
        self.logger.info('Time used: %s', datetime.timedelta(seconds=(end - start)))

    def __save_result(self, result):
        try:
            path = Path(self.score_dir)
            if not path.exists():
                path.mkdir(parents=True)
            filename = '{}/{}'.format(self.score_dir, self.workspace.result_file)
            fh = open(filename, mode='w')
            fh.write(str(result))
        except IOError:
            self.logger.error('failed to save result into {}'.format(filename))
        fh.close

    def __prepare_pressure_worker(self):
        self.logger.info('prepare pressure docker')
        path = Path(self.workspace.home)
        if not path.exists():
            path.mkdir(parents=True)
        script = """
            cd {home}
            # empty dir
            if [[ "$(ls -A {home})" ]]; then 
                rm -r *
            else
                echo '{home} is clean'
            fi      
            git clone {repo}
            cd {repo_name}
            git checkout journalq_b
            if [[ "$(ls -A {benchmark})" ]]; then 
                cp -r {benchmark}/* ./
            else
                echo 'use default benchmark config'
            fi
            mvn -P docker install 
        """.format(home=self.workspace.home,
                   repo=self.task.pressure_repo,
                   repo_name=self.task.pressure_repo_name,
                   benchmark=self.benchmark_config).rstrip()
        code, outs, _ = self.__run_local_script(script)
        if code != 0:
            raise WorkflowError(
                'Failed to prepare pressure docker {}'.format(self.task.pressure_repo),
                error_code=FAILED_TO_PREPARE_PRESSURE)

    def __config_pressure_worker(self):
        if self.benchmark_config is not None:
            script = """
            if [[ "$(ls -A {benchmark})" ]]; then 
                scp 
            else
                echo 'use default benchmark config'
            fi      
        """.format(benchmark=self.benchmark_config, repo=self.task.pressure_repo,
                   repo_name=self.task.pressure_repo_name).rstrip()

    def __start_pressure_worker(self, driver_name='journalq'):
        script = """
                    docker run --name {} {}/{} bin/benchmark -d driver-{}/{}.yaml  workloads/{}
                 """.format(self.__pressure_container_name(), self.task.pressure_docker_namespace,
                            self.task.pressure_repo_name,driver_name, driver_name, self.__list_workloads())
        code, outs, _ = self.__run_local_script(script)
        if code != 0:
            raise WorkflowError(
                'Failed to start pressure docker {}'.format(self.task.pressure_repo),
                error_code=FAILED_TO_PREPARE_PRESSURE)

    def __collect_data_from_pressure_worker(self):
        script = """
            containerId=$(docker ps -a|grep {repo_name}|grep {label_name}|awk '{{print $1}}')
            if [[ -n $containerId ]]; then
                docker cp  $containerId:/benchmark {home}
            else 
                echo '{repo_name} docker not found,failed to collect pressure result'
                exit 1    
            fi
        """.format(repo_name=self.task.pressure_repo_name, label_name=self.pressure_container_name ,home=self.workspace.home)
        self.__run_local_script(script)

    def __collect_data(self, sub_dir='benchmark'):
        self.logger.info('>>> Collect data.')
        results_dir = '{}/{}'.format(self.workspace.home, sub_dir)
        path = Path(results_dir)
        if not path.exists():
            return 'benchmark result not exist'
        files = os.listdir(results_dir)
        scores = "Openmessaging benchmark:\n\n"
        for name in files:
            if name.endswith('.json'):
                fh = open('{}/{}'.format(results_dir, name), 'r')
                score = fh.read()
                jsonScore = json.loads(score)
                scores += self.__format_perf(jsonScore)
        return scores

    def __list_workloads(self):
        workloads_dir = "{}/{}/workloads".format(self.workspace.home, self.task.pressure_repo_name)
        files = os.listdir(workloads_dir)
        self.logger.info(' workloads/'.join(files))
        return ' workloads/'.join(files)

    def __prepare_mq_worker(self):
        if self.mode != ONLINE_MODE:
            return
        # check docker images
        script = """
            imageId=$(docker images |grep {}|awk '{{print $3}}'|uniq)
            if [[ -z $imageId ]]; then
               echo 'docker image {} no exist'
               exit 1
            else
               echo $imageId
            fi
            exit 0
        """.format(self.task.mq_repo_name, self.task.mq_repo_name).rstrip()
        code, outs, _ = self.__run_local_script(script)
        if code != 0:
            raise WorkflowError(
                'Failed to prepare mq docker {}'.format(self.task.mq_repo_name),
                error_code=FAILED_TO_PREPARE_MQ_DOCKER)
        self.mq_image_id = str(outs,encoding='utf-8').replace("\n", "")

        # save to jar file
        mq_docker_tar = "{image_namespace}_{image_name}_{image_id}{format}".format(image_id=self.mq_image_id,image_name=self.task.mq_repo_name,
                                                                 image_namespace=self.task.mq_docker_namespace,format='.tar');
        script = "docker save {image_id} > {image_name}".format(image_id=self.mq_image_id, image_name=mq_docker_tar).rstrip()
        code, outs, _ = self.__run_local_script(script)
        if code != 0:
            raise WorkflowError('Failed to save mq docker {}'.format(self.task.mq_repo_name),
                                error_code=FAILED_TO_PREPARE_MQ_DOCKER)

        # scp to remote cluster
        self.logger.info("scp docker file to remote:"+','.join(self.workspace.cluster_hosts))
        for h in self.workspace.cluster_hosts:
            self.__local_scp(mq_docker_tar, h, self.workspace.home)

        # load local docker jar to docker 
        self.logger.info("start load remote docker file")
        for h in self.workspace.cluster_hosts:
            self.__load_remote_docker_file(h, self.workspace.home, mq_docker_tar, self.mq_image_id,
                                           self.task.mq_docker_namespace, self.task.mq_repo_name, 'latest')

    def __local_scp(self, file, remote, target_dir):
        passwd = self.__parse_password(self.ssh_passwd_file)
        ssh_pass = """ sshpass -p {}""".format(passwd).rstrip()
        scp = """ scp -P {} {} {}@{}:{} """.format(self.ssh_port,file, self.ssh_user, remote, target_dir)
        if passwd is not None:
            scp = ssh_pass+scp
        code, outs, _ = self.__run_local_script(scp)
        if code != 0:
            raise WorkflowError('Failed to prepare mq docker {}'.format(self.task.mq_repo_name),
                                error_code=FAILED_TO_PREPARE_MQ_DOCKER)

    def __load_remote_docker_file(self, remote, path, file_name, image_id, namespace, repo, tag):
        script = """
             docker load < {}/{}
             docker tag {} {}/{}:{}
        """.format(path, file_name, image_id, namespace, repo, tag).rstrip()
        code, outs, _ = self.__run_remote_script(remote, script)
        if code != 0:
            raise WorkflowError(
                'Failed to load {} mq docker {}'.format(remote, self.task.mq_repo_name),
                error_code=FAILED_TO_PREPARE_MQ_DOCKER)

    def __start_mq_cluster_workers(self):
        # local can't be one of worker
        for h in self.workspace.cluster_hosts:
            self.__invoke_mq_worker(h)

    def __check_mq_cluster_state(self):

        for h in self.workspace.cluster_hosts:
            code, outs, _ = self.__check_mq_stat(h)
            if code != 0:
                raise WorkflowError(
                    'Failed to start {} mq docker {}'.format(h, self.task.mq_repo_name),
                    error_code=FAILED_TO_INVOKE_MQ_DOCKER)
            else:
                code, outs, _ = self.__mq_container_id(h)
                if code != 0:
                    self.logger.info(h+' mq container id lookup failed! ')
                else:
                    self.running_mq_containers[h] = str(outs, encoding='utf-8').replace('\n', '')

    def __pressure_container_name(self):
        length = len(self.running_mq_containers)
        if length > 0:
            self.pressure_container_name = '_'.join(self.running_mq_containers.values())
        else:
            self.pressure_container_name = self.task.pressure_repo_name + str(time.time()).replace('.', '_')
        return self.pressure_container_name

    def __check_mq_stat(self, host, max_attempts=10, sleep=5):
        script = """
                containerId=$(docker ps -a|grep {docker_namespace}/{repo_name}|grep {tag}|awk '{{print $1}}')
                if [[ -z $containerId ]]; then
                    echo 'docker container {repo_name} no exist,something wrong'
                    exit 1
                else
                    ATTEMPTS=0
                    MAX_ATTEMPTS={max_attempts}
                    containerId=$(echo "$containerId"|sed ':a;N;$!ba;s/\\n/ /g') 
                    echo "docker exec $containerId cat logs/debug.log|grep 'JournalQ is started'"
                    while true; do
                        started=$(docker exec $containerId cat logs/debug.log|grep 'JournalQ is started')
                        echo "check state: $started"
                        if [[ -n $started ]]; then
                           exit 0
                        elif [[ $ATTEMPTS -eq $MAX_ATTEMPTS ]]; then
                            echo "mq instance not started after $ATTEMPTS attempts."
                            docker cp $containerId:logs/debug.log {workspace_home}
                            exit 1
                        fi   
                        ATTEMPTS=$((ATTEMPTS+1))
                        sleep {sleep} 
                    done  
                fi
        """.format(workspace_home=self.workspace.home,docker_namespace=self.task.mq_docker_namespace,
                   repo_name=self.task.mq_repo_name,
                   mq_home=self.task.mq_home,
                   max_attempts=max_attempts,
                   sleep=sleep,
                   tag=self.mq_tag).rstrip()
        return self.__run_remote_script(host, script)

    def __mq_container_id(self,host):
        script = """
                containerId=$(docker ps -a|grep {docker_namespace}/{repo_name}|grep {tag}|awk '{{print $1}}')
                if [[ -z $containerId ]]; then
                    echo 'docker container {repo_name} no exist,something wrong'
                    exit 1
                else
                    containerId=$(echo "$containerId"|sed ':a;N;$!ba;s/\\n/ /g') 
                    echo "$containerId" 
                fi
        """.format(docker_namespace=self.task.mq_docker_namespace,
                   repo_name=self.task.mq_repo_name,
                   mq_home=self.task.mq_home,
                   tag=self.mq_tag).rstrip()
        return self.__run_remote_script(host, script)

    def __invoke_mq_worker(self, host):
        script = """
                if [[ -d {mq_home} ]]; then
                    rm -r {mq_home}
                else
                    echo '{mq_home} is clean'  
                fi
                docker run --network host --name {tag} --label cluster={tag} -v {mq_home}:{mq_home} -p 50088:50088 -p 50089:50089 -p 50090:50090 -p 50091:50091 -d {mq_docker_namespace}/{mq_docker_name} bin/startmq_docker.sh
        """.format(mq_home=self.task.mq_home,
                   mq_docker_namespace=self.task.mq_docker_namespace,
                   mq_docker_name=self.task.mq_repo_name,
                   tag=self.mq_tag).rstrip()
        code, outs, _ = self.__run_remote_script(host, script)
        if code != 0:
            raise WorkflowError(
                'Failed to invoke mq docker {} on {}'.format(self.task.mq_repo_name, host),
                error_code=FAILED_TO_INVOKE_MQ_DOCKER)

    def __shutdown_and_cleanup_remote_mq_worker(self, host):
        script = self.__shutdown_cleanup_mq_worker_script()
        code, outs, _ = self.__run_remote_script(host, script)
        MAX = 5
        if code != 0:
            self.logger.error("failed to stop {} mq worker".format(host))

    def __shutdown_cleanup_mq_worker_script(self):
        script = """
                containerId=$(docker ps -a|grep {docker_namespace}/{repo_name}|grep {tag}|awk '{{print $1}}')
                if [[ -z $containerId ]]; then
                    echo 'docker container {repo_name} no exist'
                else
                    echo "{repo_name} container:$containerId"
                    docker stop $containerId
                    docker rm -f $containerId
                    echo 'try again'
                    docker stop $containerId
                    docker rm -f $containerId
                fi
                imageId=$(docker images |grep {repo_name}|awk '{{print $3}}'|uniq)
                if [[ -z $imageId ]]; then
                    echo 'docker image {repo_name} no exist'
                else
                    echo "{repo_name} images id: $imageId"
                    docker rmi -f $imageId
                fi
                if [[ -d {mq_home} ]]; then
                   echo  'delete {mq_home}'
                   rm -r {mq_home}
                else
                   echo  {mq_home} not exist    
                fi    
            """.format(docker_namespace=self.task.mq_docker_namespace,
                       repo_name=self.task.mq_repo_name,
                       mq_home=self.task.mq_home,
                       tag=self.mq_tag).rstrip()
        return script

    def __cleanup_local_docker(self):
        # script = self.__shutdown_cleanup_mq_worker_script()
        script = """
                # docker system prune -f
                mq_image_id=$(docker images |grep {mq_repo_name}|awk '{{print $3}}'|uniq)
                if [[ -n $mq_image_id ]]; then
                    docker rmi -f $mq_image_id
                else
                    echo 'docker image {mq_repo_name} not exist'
                fi
                pressure_image_id=$(docker images |grep {pressure_repo_name}|awk '{{print $3}}'|uniq)
                if [[ -n $pressure_image_id ]]; then
                    docker rmi -f $pressure_image_id
                else
                    echo 'docker image {pressure_repo_name} not exist'
                fi
                docker images
                docker ps -a 
        """.format(mq_repo_name=self.task.mq_repo_name, pressure_repo_name=self.task.pressure_repo_name,mq_home=self.task.mq_home).rstrip()
        self.__run_local_script(script)

    def __cleanup_workspace(self):
        script = """
                if [[ -d {workspace_home}/{pressure_repo_name} ]]; then
                    rm -r {workspace_home}/{pressure_repo_name}
                fi
                if [[ -d {workspace_home}/benchmark ]]; then
                    rm -r {workspace_home}/benchmark
                fi
                ls -l {workspace_home}
        """.format(workspace_home=self.workspace.home,pressure_repo_name=self.task.pressure_repo_name, mq_home=self.task.mq_home).rstrip()
        self.__run_local_script(script)

    def __mock_crash_recovery(self, time_ms=1000):
        self.__random_kill_mq_cluster_work()
        self.logger.info('will sleep '+time_ms)
        time.sleep(time_ms)
        self.__recovery_mq_cluster_work()

    def __random_kill_mq_cluster_work(self):
        hosts = self.workspace.cluster_hosts
        l = len(hosts)
        ind = random.randint(0, l)
        host = hosts[ind]
        script = """
                containerId = $(ps -ef|grep {}/{}|awk '{{print $1}}')
                docker stop $containerId
        """.format(self.task.mq_docker_namespace, self.task.mq_repo_name).rstrip()
        container_id = self.__run_remote_script(host, script)
        self.logger.info('{},container id:{} stoped '.format(host, container_id))
        self.killed.host = host
        self.killed.container_id =container_id
        return container_id

    def __recovery_mq_cluster_work(self):
        script = """
                docker start $containerId
        """.format(self.killed.container_id).rstrip()
        self.__run_remote_script(self.killed.host, script)
        self.logger.info('{},container id:{} started '.format(self.killed.host,self.killed.container_id))

    def __shutdown_and_cleanup_local(self):

        self.__run_local_script("stop all mq server")

    def __shutdown_and_cleanup_mq_cluster_workers(self):
        for h in self.workspace.cluster_hosts:
            self.__shutdown_and_cleanup_remote_mq_worker(h)

    def __run_script(self, bash, script):
        self.logger.debug('Script to execute:\n%s\n', script)
        with subprocess.Popen(
                bash,
                stdin=subprocess.PIPE,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT
        ) as proc:
            outs, errs = proc.communicate(script.encode('utf-8'))
            code = proc.returncode
            self.logger.debug('Return code = %s', code)
            self.logger.debug('The output is as following:\n%s',str(outs, encoding= 'utf-8'))
            return code, outs, errs

    def __run_local_script(self, script):
        return self.__run_script(['/bin/bash'], script)

    def __run_remote_script(self, remote, script):
        passwd = self.__parse_password(self.ssh_passwd_file)
        ssh_pass = """ sshpass -p {}""".format(passwd).rstrip()
        ssh = 'ssh {}@{} -p {}'.format(self.ssh_user, remote,self.ssh_port)
        # if password exist
        if passwd is not None:
            ssh = ssh_pass+ssh
        bash = ['/bin/bash']
        if self.mode == ONLINE_MODE:
            bash = split(ssh) + bash
        return self.__run_script(bash, script)

    def __lock_local_workspace(self):
        self.logger.info('>>> Lock local workspace.')


    def __parse_password(self, filename):
        file = Path(filename)
        if not file.exists():
            return None
        fh = open(filename, 'r')
        return fh.readline()

    def __format_perf(self, performance):
        p = performance
        table_name = p['driver'] + p['workload']+'\n\n'
        pub = p['publishRate']
        con = p['consumeRate']
        pub_latencyMax = p['publishLatencyMax']
        pub_latencyAvg = p['publishLatencyAvg']
        pub_latency50pct = p['publishLatency50pct']
        pub_latency75pct = p['publishLatency75pct']
        pub_latency95pct = p['publishLatency95pct']
        pub_latency99pct = p['publishLatency99pct']
        pub_latency999pct = p['publishLatency999pct']
        pub_latency9999pct = p['publishLatency9999pct']
        header = ['Pub rate(msg/s)', 'Cons rate(msg/s)', 'Tpmax(ms)', 'Tpavg(ms)', 'Tp50(ms)', 'Tp75(ms)', 'Tp99(ms)', 'Tp999(ms)', 'Tp999(ms)']
        rows = []
        for i in range(len(p['publishRate'])):
            row = [int(pub[i]), int(con[i]), pub_latencyMax[i], pub_latencyAvg[i], pub_latency50pct[i], pub_latency75pct[i],
                   pub_latency95pct[i], pub_latency99pct[i], pub_latency999pct[i], pub_latency9999pct[i]]
            rows.append(row)
        return self.__markdown_table(table_name, header, rows)

    def __markdown_table(self, table_name, header, rows):
        table = table_name
        table += '|'+'|'.join(header)+'|\n'
        align = '|'
        for i in header:
            align += ':---:|'
        table += align+'\n'
        for row in rows:
            table += '|'+'|'.join(map(str, row))+'|\n'
        return table

    def __cleanup(self):
        self.logger.info('>>> start to clean up workspace.')
        self.__shutdown_and_cleanup_mq_cluster_workers()
        self.__cleanup_local_docker()
        self.__cleanup_workspace()
        self.__unlock_local_task_home()

    def __unlock_local_task_home(self):
        self.logger.info('>>> Unlock local workspace.')


class WorkflowError(Exception):

    def __init__(self, message, error_code=9999):
        self.message = message
        self.error_code = error_code


