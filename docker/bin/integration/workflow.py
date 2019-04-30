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
from shlex import split
from pathlib import Path
import os
import datetime
import logging
import time
from model.task import Task
from model.workspace import Workspace

logging.basicConfig()
FAILED_TO_LOCK_LOCAL_WORKSPACE = 501
FAILED_TO_PREPARE_PRESSURE = 502
FAILED_TO_PREPARE_MQ_DOCKER = 503
RESULT_FILE = 'result.json'


class Workflow:

    def __init__(self, config):
        self.config = config;
        self.task = Task(config)
        self.workspace = Workspace(config)
        self.ssh_user = config.get_value('User', 'SSH')
        self.ssh_passwd_file = config.get_value('PasswdFile', 'SSH')
        self.ssh_port = config.get_value('Port', 'SSH')
        self.pwd = os.path.dirname(__file__)
        self.logger = logging.getLogger(__name__)
        self.logger.setLevel(logging.DEBUG)
        self.logger.info('local workspace = %s', "localhost")
        self.logger.info('remote workspace = %s', "172.28.123.213")

    def run(self):
        start = time.time()
        result = None
        try:
            self.__lock_local_workspace()
            self.__prepare_pressure_worker()
            self.__prepare_mq_worker()
            self.__invoke_mq_cluster_works()
            self.__start_pressure_worker()
            self.__random_kill_mq_cluster_work()
            self.__recovery_mq_cluster_work()

        except WorkflowError as err:
            result = {
                'status': -err.error_code,
                'is_valid': 1 ,
                'message': err.message,

            }
            self.logger.exception('Failed to execute workflow.')
        finally:
            self.logger.info('close')
        end = time.time()
        self.logger.info('Time used: %s', datetime.timedelta(seconds=(end - start)))
        if result is None:
            result = {
                'status': 0,
                'is_valid': 1,
                'message': 'Success',
            }
        self.__save_result_to_local(result, self.workspace.result_file)

    def __save_result_to_local(self, result, filename= RESULT_FILE):
        try:
            fh = open(filename, mode='w')
            fh.write(str(result))
        except IOError:
            self.logger.err('Error: 没有找到文件或读取文件失败')
        fh.close

    def __prepare_pressure_worker(self):
        self.logger.info('prepare pressure docker')
        path = Path(self.workspace.home)
        if not path.exists():
            path.mkdir(parents=True)
        script = """
            cd {home}
            # empty dir 
            rm -r *  
            git clone {repo}
            cd {repo_name}
            mvn -P artifactory,docker install 
        """.format(home=self.workspace.home, repo=self.task.pressure_repo,
                   repo_name=self.task.pressure_repo_name).rstrip()
        returncode, outs, _ = self.__run_local_script(script)
        if returncode != 0:
            raise WorkflowError(
                'Failed to prepare pressure docker {}'.format(self.task.pressure_repo),
                error_code=FAILED_TO_PREPARE_PRESSURE)

    def __start_pressure_worker(self):
        script = """
            echo 'start pressure!'
            exit 0
        """
        self.__run_local_script(script)

    def __prepare_mq_worker(self):
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
        # save
        mq_docker_tar = "{image_name}_{image_id}{format}".format(image_id=self.mq_image_id,image_name=self.task.mq_repo_name, format='.tar');
        script = "docker save {image_id} > {image_name}".format(image_id=self.mq_image_id, image_name=mq_docker_tar).rstrip()
        code, outs, _ = self.__run_local_script(script)
        if code != 0:
            raise WorkflowError(
                'Failed to save mq docker {}'.format(self.task.mq_repo_name),
                error_code=FAILED_TO_PREPARE_MQ_DOCKER)
        # scp
        hosts = self.workspace.cluster.split(',')
        self.logger.info("scp docker file to remote:"+','.join(hosts))
        for h in hosts:
            self.__scp(mq_docker_tar, h, no_passwd=False)
        # load
        self.logger.info("start load remote docker file")
        for h in hosts:
            self.__load_remote_docker_file(h, self.workspace.home,mq_docker_tar,self.mq_image_id, self.task.mq_repo_name, 'latest')

    def __scp(self, file, remote, no_passwd =True):
        passwd_script = """ sshpass -p $(cat {})""".format(self.ssh_passwd_file).rstrip()
        script_scp = """ scp -P {} {} {}@{}:{} """.format(self.ssh_port,file, self.ssh_user, remote, self.workspace.home)
        script = script_scp
        if not no_passwd:
           script = passwd_script+script_scp
        code, outs, _ = self.__run_local_script(script)
        if code != 0:
            raise WorkflowError(
                'Failed to prepare mq docker {}'.format(self.task.mq_repo_name),
                error_code=FAILED_TO_PREPARE_MQ_DOCKER)

    def __load_remote_docker_file(self, remote, path, file_name, image_id, repo, tag):
        script = """
             docker load < {}/{}
             docker tag {} {}:{}
        """.format(path, file_name, image_id, repo, tag).rstrip()
        code, outs, _ = self.__run_remote_script(remote, script)
        if code != 0:
            raise WorkflowError(
                'Failed to load {} mq docker {}'.format(remote, self.task.mq_repo_name),
                error_code=FAILED_TO_PREPARE_MQ_DOCKER)

    def __invoke_mq_cluster_works(self):
        script = """
                echo 'start mq cluster '
        """
        self.__run_local_script(script)

    def __random_kill_mq_cluster_work(self):
        script = """
            echo 'random kill mq cluster!'
            exit 0
        """
        self.__run_local_script(script)

    def __recovery_mq_cluster_work(self):
        self.__run_local_script("save scp and load")

    def __stop_services(self):
        self.__run_local_script("stop all mq server")

    def __check_pressure_services(self):
        self.__run_local_script("stop all mq server")

    def __run_script(self, bash, script):
        self.logger.debug('Script to execute:\n%s\n', script)
        with subprocess.Popen(
                bash,
                stdin=subprocess.PIPE,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT
        ) as proc:
            outs, errs = proc.communicate(script.encode('utf-8'))
            returncode = proc.returncode
            self.logger.debug('Return code = %s', returncode)
            self.logger.debug('The output is as following:\n%s',str(outs, encoding = 'utf-8'))
            return returncode, outs, errs

    def __run_local_script(self, script):
        return self.__run_script(['/bin/bash'], script)

    def __run_remote_script(self, remote, script):
        passwd= self.__cat_password(self.ssh_passwd_file)
        self.logger.debug('Remote Script to execute:\n%s\n', script)
        ssh = 'sshpass -p {} ssh {}@{} -p {}'.format(passwd, self.ssh_user, remote,self.ssh_port)
        bash = split(ssh) + ['/bin/bash']
        return self.__run_script(bash, script)

    def __lock_local_workspace(self):
        self.logger.info('>>> Lock local workspace.')

    def __cat_password(self, file):
        fh = open(file, 'r')
        return fh.readline()

    def __remove_cluster_docker_images(self):
        self.__run_remote_script("remove cluster dockers images and local data");

    def __download_logs(self):
        self.logger.info("download logs")

    def __collect_data(self):
        self.logger.info('>>> Collect data.')
        self.__download_logs()

    def __cleanup(self):
        self.__unlock_local_task_home()

    def __unlock_local_task_home(self):
        self.logger.info('>>> Unlock local workspace.')


class WorkflowError(Exception):

    def __init__(self, message, error_code=9999):
        self.message = message
        self.error_code = error_code


