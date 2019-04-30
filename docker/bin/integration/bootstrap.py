# -*- coding:utf-8 -*-
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
import os
from workflow import Workflow
from configuration import Configuration


def bootstrap():
    config_file = os.path.dirname(__file__)+'/bootstrap.conf'
    configuration = Configuration(config_file)
    workflow = Workflow(configuration)
    workflow.run()


if __name__ == '__main__':
    bootstrap();
