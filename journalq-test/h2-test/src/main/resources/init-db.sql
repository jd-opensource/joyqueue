--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE message_retry (
id bigint(20) NOT NULL AUTO_INCREMENT,
message_id varchar(50) NOT NULL,
business_id varchar(100) DEFAULT NULL,
topic varchar(100) NOT NULL,
app varchar(100) NOT NULL,
send_time datetime NOT NULL,
expire_time datetime NOT NULL,
retry_time datetime NOT NULL,
retry_count int(10) NOT NULL DEFAULT '0',
data mediumblob NOT NULL,
exception blob,
create_time datetime NOT NULL,
create_by int(10) NOT NULL DEFAULT '0',
update_time datetime NOT NULL,
update_by int(10) NOT NULL DEFAULT '0',
status tinyint(4) NOT NULL DEFAULT '1',
PRIMARY KEY (id)
) AUTO_INCREMENT = 0;