package org.levi.engine.impl;

import org.levi.engine.*;
import org.levi.engine.identity.IdentityService;
import org.levi.engine.utils.LeviUtils;
import org.levi.engine.utils.ObjectLoader;
import org.levi.engine.utils.ObjectSaver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Ishan Jayawardena
 */
public class ProcessEngineImpl implements ProcessEngine {

    private EngineData engineData;
    private StorageService storageService;
    private RuntimeService runtimeService;
    private TaskService taskService = new TaskServiceImpl();
    private IdentityService identityService = new IdentityServiceImpl();
    
    private static final ProcessEngine processEngine = new ProcessEngineImpl();
    //private static Logger log = Logger.getLogger(ProcessEngineImpl.class);

    private ProcessEngineImpl() {    // todo <== process engine configuration
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static ProcessEngine getProcessEngine() {
        return processEngine;
    }

    private void start()
            throws IOException, ClassNotFoundException {
        File leviHome = new File(Constants.LEVI_HOME);
        if (!leviHome.exists()) {
            leviHome.mkdir();
        }
        File lomPath = new File(Constants.LOM_PATH);
        if (!lomPath.exists()) {
            lomPath.mkdir();    
        }
        File extractPath = new File(Constants.LAR_EXTRACT_PATH);
        if (!extractPath.exists()) {
            extractPath.mkdir();
        }
        File engineDataFile = new File(Constants.ENGINEDATA_PATH);
        if (engineDataFile.exists()) {
            ObjectLoader loader = new ObjectLoader(Constants.ENGINEDATA_PATH);
            EngineData engineData = (EngineData) loader.readNextObject();
            if (engineData == null) {
                throw new RuntimeException("Retrieved engine data is null.");
            }
            this.engineData = engineData;
        } else {
            engineData = new EngineData();
        }
        storageService = new StorageServiceImpl(engineData);
        storageService.start();
        runtimeService = new RuntimeServiceImpl(engineData);
        runtimeService.start();
    }

    public synchronized List<String> getDeploymentIds() {
        return LeviUtils.giveList(engineData.getDeploymentIds());
    }

    public synchronized List<String> getRunningProcessIds() {
        return LeviUtils.giveList(engineData.getRunningProcessIds());
    }

    public synchronized void stop()
            throws FileNotFoundException, IOException {
        File f = new File(Constants.ENGINEDATA_PATH);
        if (f.exists()) {
            f.delete();
        } // todo this is too bad! fix it
        ObjectSaver saver = new ObjectSaver(Constants.ENGINEDATA_PATH);
        saver.saveObject(this.engineData);
        storageService.stop();
        runtimeService.stop();
    }

    public synchronized String deploy(String larPath) throws IOException {
        if (larPath == null) {
            throw new LeviException("Lar path is null.");
        }
        Deployment d = storageService.createDeployment(larPath).deploy();
        return d.getDefinitionsId();
    }

    public void undeployAll() throws IOException {
        storageService.undeployAll();
    }

    public synchronized void undeploy(String id) throws IOException {
        if (id == null) {
            throw new LeviException("Process ID is null.");
        }
        storageService.undeploy(id);
    }

    public void cleanup() throws IOException {
        storageService.cleanup();
    }

    public synchronized void startProcess(String id, Map<String, Object> variables)
            throws IOException, ClassNotFoundException {
        if (id == null) {
            throw new LeviException("Process ID is null.");
        }
        try {
            runtimeService.startProcess(id, variables);
        } catch (Exception e) {
            // cleanup
            cleanup();
            throw new LeviException(e);
        }
    }

    public synchronized void stopProcess(String id)
            throws IOException, ClassNotFoundException {
        if (id == null) {
            throw new LeviException("Process ID is null.");
        }
        runtimeService.stopProcess(id);
    }

    public String getDiagramPath(String id) {
        if (id == null) {
            throw new LeviException("Process definitions id is null");
        }
        return storageService.getDiagramPath(id);
    }
}