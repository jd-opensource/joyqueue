package org.joyqueue.broker.config.scan;


import java.util.Set;
import java.util.function.Predicate;

public class ScannerExecutor implements Scanner {

    private static volatile ScannerExecutor instance;

    @Override
    public Set<Class<?>> search(String packageName, Predicate<Class<?>> predicate) {
        Scanner fileSc = new FileScanner();
        Set<Class<?>> fileSearch = fileSc.search(packageName, predicate);
        Scanner jarScanner = new JarScanner();
        Set<Class<?>> jarSearch = jarScanner.search(packageName,predicate);
        fileSearch.addAll(jarSearch);
        return fileSearch;
    }

    private ScannerExecutor(){}

    public static ScannerExecutor getInstance(){
        if(instance == null){
            synchronized (ScannerExecutor.class){
                if(instance == null){
                    instance = new ScannerExecutor();
                }
            }
        }
        return instance;
    }

}