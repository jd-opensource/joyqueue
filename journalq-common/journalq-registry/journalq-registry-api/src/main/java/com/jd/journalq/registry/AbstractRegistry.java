package com.jd.journalq.registry;

import com.jd.journalq.registry.listener.*;
import com.jd.journalq.toolkit.URL;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.registry.listener.*;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * 抽象的注册中心
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 12-12-16 下午1:30
 */
public abstract class AbstractRegistry extends Service implements Registry {
    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public void setUrl(URL url) {
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void create(String path, byte[] data) throws RegistryException {
    }

    @Override
    public void create(List<String> path) throws RegistryException {
    }

    @Override
    public void createLive(String path, byte[] data) {
    }

    @Override
    public void deleteLive(String path) {
    }

    @Override
    public Lock createLock(String path) {
        return null;
    }

    @Override
    public void update(String path, byte[] data) throws RegistryException {
    }

    @Override
    public void update(PathData data) throws RegistryException {

    }

    @Override
    public void update(String path, byte[] data, byte[] parent) throws RegistryException {
    }

    @Override
    public void delete(String path) throws RegistryException {
    }

    @Override
    public void delete(List<String> path) throws RegistryException {
    }

    @Override
    public boolean exists(String path) throws RegistryException {
        return false;
    }

    @Override
    public boolean isLeader(String path) throws RegistryException {
        return false;
    }

    @Override
    public PathData getData(String path) throws RegistryException {
        return null;
    }

    @Override
    public List<PathData> getChildData(String path) throws RegistryException {
        return null;
    }

    @Override
    public List<String> getChildren(String path) throws RegistryException {
        return null;
    }

    @Override
    public void addListener(String path, ChildrenListener listener) {
    }

    @Override
    public void addListener(String path, ChildrenDataListener listener) {
    }

    @Override
    public void addListener(String path, PathListener listener) {
    }

    @Override
    public void addListener(String path, LeaderListener listener) {
    }

    @Override
    public void addListener(String path, ClusterListener listener) {
    }

    @Override
    public void addListener(ConnectionListener listener) {
    }

    @Override
    public void removeListener(String path, PathListener listener) {
    }

    @Override
    public void removeListener(String path, ChildrenListener listener) {
    }

    @Override
    public void removeListener(String path, ChildrenDataListener listener) {
    }

    @Override
    public void removeListener(String path, LeaderListener listener) {
    }

    @Override
    public void removeListener(String path, ClusterListener listener) {
    }

    @Override
    public void removeListener(ConnectionListener listener) {
    }

    @Override
    public String type() {
        return null;
    }
}
