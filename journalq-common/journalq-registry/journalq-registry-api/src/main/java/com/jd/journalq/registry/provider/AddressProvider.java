/**
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
package com.jd.journalq.registry.provider;


import com.jd.journalq.toolkit.UrlAware;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.Type;

/**
 * 注册中心工厂类
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 12-12-12 下午4:09
 */
public interface AddressProvider extends Type, UrlAware {
    ExtensionPoint<AddressProvider, String> ADDRESSPROVIDER = new ExtensionPointLazy<>(AddressProvider.class);

    /**
     * 获取地址
     *
     * @return 地址
     */
    String getAddress() throws Exception;

}
