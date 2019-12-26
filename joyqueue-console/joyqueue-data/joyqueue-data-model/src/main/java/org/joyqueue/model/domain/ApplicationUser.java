/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.model.domain;

/**
 * Created by yangyang115 on 18-9-11.
 */
public class ApplicationUser extends BaseModel {

    private Identity application;

    private Identity user;

    public ApplicationUser() {
    }

    public ApplicationUser(Identity application, Identity user) {
        this.application = application;
        this.user = user;
    }

    public Identity getApplication() {
        return application;
    }

    public void setApplication(Identity application) {
        this.application = application;
    }

    public Identity getUser() {
        return user;
    }

    public void setUser(Identity user) {
        this.user = user;
    }
}
