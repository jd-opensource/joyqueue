import locale from '../locale/index'
import { Row, Col } from './grid'
import { Container, Header, Content, Footer, Sider } from './layout'
import Icon from './icon'
import { Button, ButtonGroup } from './button'
import { Radio, RadioGroup } from './radio'
import { Checkbox, CheckboxGroup } from './checkbox'
import { Select, Option, OptionGroup } from './select'
import Input from './input'
import { DatePicker, TimePicker, TimeSelect } from './date-picker'
import { Menu, MenuItem, MenuGroup, Submenu } from './menu'
import { Dialog, StatusDialog } from './dialog'
import { Form, FormItem } from './form'
import Tabs from './tabs'
import TabPane from './tab-pane'
import Pagination from './pagination'
import Table from './table'
import { Breadcrumb, BreadcrumbItem } from './breadcrumb'
import { Dropdown, DropdownItem, DropdownMenu } from './dropdown'
import { Step, Steps } from './step'
import Autocomplete from './autocomplete'
import Message from './message'
import Loading from './loading'
import Switch from './switch'
import Spin from './spin'
import Notification from './notification'
import Tag from './tag'
import Tooltip from './tooltip'
import Tree from './tree'

const components = [
  Row,
  Col,
  Container,
  Header,
  Sider,
  Content,
  Footer,
  Icon,
  Button,
  ButtonGroup,
  Radio,
  RadioGroup,
  Checkbox,
  CheckboxGroup,
  Select,
  Option,
  OptionGroup,
  Input,
  DatePicker,
  TimePicker,
  TimeSelect,
  Menu,
  MenuItem,
  MenuGroup,
  Submenu,
  Dialog,
  Form,
  FormItem,
  Tabs,
  TabPane,
  Pagination,
  Table,
  Breadcrumb,
  BreadcrumbItem,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  Step,
  Steps,
  Autocomplete,
  Loading,
  Switch,
  Spin,
  Notification,
  Tag,
  Tooltip,
  Tree
]

const install = function (Vue, opts = {}) {
  if (install.installed) return
  locale.use(opts.locale)
  locale.i18n(opts.i18n)

  components.map(component => {
    Vue.component(component.name, component)
  })

  Vue.prototype.$Dialog = StatusDialog
  Vue.prototype.$Message = Message
  Vue.prototype.$Notice = Notification
  Vue.prototype.$Loading = Loading
  Vue.prototype.$Spin = Spin

  Vue.prototype.$DUI = {
    size: opts.size || '',
    transfer: 'transfer' in opts ? opts.transfer : ''
  }
}

/**
 * Global Install
 */
if (typeof window !== 'undefined' && window.Vue) {
  install(window.Vue)
}

export default {
  version: process.env.VERSION,
  locale: locale.use,
  i18n: locale.i18n,
  install,
  Row,
  Col,
  Container,
  Header,
  Sider,
  Content,
  Footer,
  Icon,
  Button,
  ButtonGroup,
  Radio,
  RadioGroup,
  Checkbox,
  CheckboxGroup,
  Select,
  Option,
  OptionGroup,
  Input,
  DatePicker,
  TimePicker,
  TimeSelect,
  Menu,
  MenuItem,
  MenuGroup,
  Submenu,
  Dialog,
  StatusDialog,
  Form,
  FormItem,
  Tabs,
  TabPane,
  Pagination,
  Table,
  Breadcrumb,
  BreadcrumbItem,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  Step,
  Steps,
  Message,
  Autocomplete,
  Loading,
  Switch,
  Spin,
  Notification,
  Tag,
  Tree
}
