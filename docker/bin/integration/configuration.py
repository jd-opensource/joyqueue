import configparser;
import os

DEFAULT_SECTION = 'Default'


class Configuration:
    def __init__(self,config_file):
        self.config = configparser.ConfigParser()
        self.config.read(config_file);

    def get_value(self, key, section=DEFAULT_SECTION):
        return self.config.get(section, key);

