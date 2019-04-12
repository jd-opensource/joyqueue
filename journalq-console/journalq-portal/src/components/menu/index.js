/*
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
import Menu from './menu.vue'
import MenuGroup from './menu-group.vue'
import MenuItem from './menu-item.vue'
import Submenu from './submenu.vue'

Menu.Group = MenuGroup
Menu.Item = MenuItem
Menu.Sub = Submenu

export { Menu, MenuItem, MenuGroup, Submenu }

export default Menu
