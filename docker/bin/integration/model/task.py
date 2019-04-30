
class Task:

    def __init__(self, config):
        self.mq_start_shell_entry = config.get_value('MQEntryPoint')
        self.pressure_shell_entry = config.get_value('PressureEntryPoint')
        self.pressure_repo = config.get_value('PressureRepo', 'Task')
        self.pressure_repo_name = config.get_value('PressureRepoName', 'Task')
        self.mq_repo = config.get_value('MQRepo', 'Task')
        self.mq_repo_name = config.get_value('MQRepoName', 'Task')

