
class Workspace:

    def __init__(self, config):
        self.__parse_cluster(config)
        self.home = config.get_value('Home', 'Workspace')
        self.user = config.get_value('User', 'Workspace')
        self.result_file = config.get_value('ResultFile', 'Workspace')

    def __parse_cluster(self, config):
        self.cluster_hosts = config.get_value('Cluster', 'Workspace').split(',')
