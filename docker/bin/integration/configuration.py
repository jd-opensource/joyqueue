import configparser;
import os

DEFAULT_SECTION = 'Default'


class Configuration:
    def __init__(self,config_file, argv):
        self.config = configparser.ConfigParser()
        self.config.read(config_file);
        self.argv = argv

    def get_value(self, key, section=DEFAULT_SECTION):
        return self.config.get(section, key);

    def set_value(self, key, value, section=DEFAULT_SECTION):
        self.config.set(self,section, key, value)

    def get_argv(self):
        return self.argv

