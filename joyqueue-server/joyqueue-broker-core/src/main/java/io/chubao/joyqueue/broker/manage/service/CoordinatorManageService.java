package io.chubao.joyqueue.broker.manage.service;

/**
 * CoordinatorManageService
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public interface CoordinatorManageService {

    /**
     * 初始化协调者
     *
     * @return 是否成功
     */
    boolean initCoordinator();

    /**
     * 移除协调组信息
     *
     * @param namespace 作用域
     * @param groupId 组
     * @return 是否成功
     */
    boolean removeCoordinatorGroup(String namespace, String groupId);
}