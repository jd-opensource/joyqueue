
class Task:

    def __init__(self, config):
        self.mq_start_shell_entry = config.get_value('MQEntryPoint', 'Task')
        self.pressure_shell_entry = config.get_value('PressureEntryPoint', 'Task')
        self.pressure_repo = config.get_value('PressureRepo', 'Task')
        self.pressure_repo_name = config.get_value('PressureRepoName', 'Task')
        self.pressure_docker_namespace = config.get_value('PressureDockerNamespace', 'Task')
        self.mq_repo = config.get_value('MQRepo', 'Task')
        self.mq_docker_namespace = config.get_value('MQDockerNamespace', 'Task')
        self.mq_docker_tag = config.get_value('MQDockerTag', 'Task')
        self.mq_home = config.get_value('MQHome', 'Task')
        self.mq_repo_name = config.get_value('MQRepoName', 'Task')
        self.expose_port = config.get_value('expose', 'Task')
        self.mq_start_flag = config.get_value('startedFlag', 'Task')
        self.mq_log_file = config.get_value('MQLogFile', 'Task')


