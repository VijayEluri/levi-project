package org.levi.engine.impl.db;

import org.levi.engine.Deployment;
import org.levi.engine.EngineData;
import org.levi.engine.db.DBManager;
import org.levi.engine.identity.Group;
import org.levi.engine.identity.User;
import org.levi.engine.impl.bpmn.StartEvent;
import org.levi.engine.impl.bpmn.UserTask;
import org.levi.engine.persistence.hibernate.HibernateDao;
import org.levi.engine.persistence.hibernate.process.hobj.*;
import org.levi.engine.persistence.hibernate.process.ql.HqlManager;
import org.levi.engine.persistence.hibernate.user.hobj.GroupBean;
import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
import org.levi.engine.runtime.ProcessInstance;
import org.levi.engine.utils.Bean2Impl;
import org.levi.engine.utils.Impl2Bean;
import org.levi.engine.utils.LeviUtils;
import org.omg.spec.bpmn.x20100524.model.TPotentialOwner;
import org.omg.spec.bpmn.x20100524.model.TUserTask;

import java.util.*;

public class DBManagerImpl implements DBManager {

    HibernateDao dao;
    HqlManager qlManager;

    public DBManagerImpl() {
        dao = new HibernateDao();
        qlManager = new HqlManager();
    }

    public void saveUser(UserBean user) {
        dao.save(user);
    }

    public void saveUser(User user) {
        UserBean userBean;
        if (dao.getObject(UserBean.class, user.getUserId()) != null) {
            userBean = (UserBean) dao.getObject(UserBean.class, user.getUserId());
            dao.update(Impl2Bean.getUserBean(user, userBean, true));
        } else {
            userBean = new UserBean();
            dao.save(Impl2Bean.getUserBean(user, userBean, false));
        }
        if (user.getUserGroups() != null) {
            for (Group group : user.getUserGroups()) {
                addUserToGroup(user.getUserId(), group.getGroupId());
            }
        }
    }

    public void saveGroup(GroupBean group) {
        dao.save(group);
    }

    public void saveGroup(Group group) {
        GroupBean groupBean;
        if (dao.getObject(GroupBean.class, group.getGroupId()) != null) {
            groupBean = (GroupBean) dao.getObject(GroupBean.class, group.getGroupId());
            dao.update(Impl2Bean.getGroupBean(group, groupBean, true));
        } else {
            groupBean = new GroupBean();
            dao.save(Impl2Bean.getGroupBean(group, groupBean, false));
        }
    }

    public UserBean getUser(String userId) {
        return (UserBean) dao.getObject(UserBean.class, userId);
    }

    public GroupBean getGroup(String groupId) {
        return (GroupBean) dao.getObject(GroupBean.class, groupId);
    }

    public void addUserToGroup(String userId, String groupId) {
        UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
        GroupBean group = (GroupBean) dao.getObject(GroupBean.class, groupId);

        int id = -1;
        List<GroupBean> grps = user.getUserGroups();
        for (GroupBean grp : grps) {
            if (grp.getGroupId().equals(group.getGroupId())) {
                id = grps.indexOf(grp);
                break;
            }

        }

        if (id == -1) {
            user.getUserGroups().add(group);
        }
        dao.update(user);
    }

    public void deleteUser(String userId) {
        dao.remove(UserBean.class, userId);
    }

    public void deleteGroup(String groupId) {
        dao.remove(GroupBean.class, groupId);
    }

    public void removeUserFromGroup(String userId, String groupId) {
        dao.close();
        dao = new HibernateDao();
        GroupBean group = (GroupBean) dao.getObject(GroupBean.class, groupId);
        UserBean user = (UserBean) dao.getObject(UserBean.class, userId);

        int id = -1;

        List<GroupBean> grps = user.getUserGroups();
        for (GroupBean grp : grps) {
            if (grp.getGroupId().equals(group.getGroupId())) {
                id = grps.indexOf(grp);
                break;
            }
        }

        if (id != -1) {
            user.getUserGroups().remove(id);
        }
        dao.update(user);
    }

    public List<String> getGroupIds(String userId) {
        return null;
    }

    public void saveTask(TaskBean task) {
        dao.save(task);
    }

    public void deleteTask(String taskId) {
        dao.remove(TaskBean.class, taskId);
    }

    public void updateTask(TaskBean task) {
        dao.update(task);
    }

    public void saveProcess(DeploymentBean deployedProcess) {
        dao.save(deployedProcess);
    }

    public void deleteProcess(String processId) {
        dao.remove(DeploymentBean.class, processId);
    }

    public void saveProcessInstance(ProcessInstanceBean process) {
        dao.save(process);
    }

    public void updateProcess(DeploymentBean process) {
        dao.update(process);
    }

    public void deleteProcessInstance(String processId) {
        dao.remove(ProcessInstanceBean.class, processId);
    }

    public void updateProcessInstance(ProcessInstanceBean process) {
        dao.update(process);
    }

    public List<TaskBean> getUserTaskList(String userId) {
        List<TaskBean> list = LeviUtils.newArrayList();
        UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
        for (TaskBean task : user.getAssigned()) {
            if (task.isActive()) {
                list.add(task);
            }
        }
        return list;
    }

    public List<ProcessInstanceBean> getRunningProcessesInstancesList() {
        return null;
    }

    public List<DeploymentBean> getDeployedProcessList() {
        return null;
    }

    public UserBean getAssigneeForTask(String taskId) {
        TaskBean task = (TaskBean) dao.getObject(UserBean.class, taskId);
        return task.getAssignee();
    }

    public List<TaskBean> getActiveTasks() {
        return null;
    }

    public List<TaskBean> getUnassignedTasks() {
        return null;
    }

    public List<TaskBean> getUnassignedTasks(String groupId) {
        return qlManager.getUnassignedTasks(groupId);
    }

    public List<TaskBean> getActiveTasks(String processId) {
        return null;
    }

    public TaskBean getTaskBean(String taskId) {
        return (TaskBean)dao.getObject(TaskBean.class, taskId);
    }

    public boolean claimUserTask(String taskId, String userId) {
        TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
        if (task != null) {
            if (task.isAssigned()) {
                return false;
            }
            task.setAssigned(true);
            UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
            user.getAssigned().add(task);
            dao.update(user);
            task.setAssignee(user);
            dao.update(task);
            return true;
        } else {
            return false;
        }
    }

    public List<UserBean> getUserList() {
        return qlManager.getUserObjects();
    }

    public List<GroupBean> getGroupList() {
        return qlManager.getGroupObjects();
    }

    public List<String> getGroupIdList() {
        return qlManager.getGroupIds();
    }

    public void assignTask(String taskId, String userId) {
        TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
        task.setActive(true);
        dao.update(task);
        UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
        user.getAssigned().add(task);
        dao.update(user);
    }

    public void unassignTask(String taskId) {
        TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
        if (task != null) {
            task.setActive(false);
            dao.update(task);
        }
    }

    public void removeTask(String taskId, String userId) {
        TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
        UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
        user.getAssigned().remove(task);
        dao.update(user);
    }

    public EngineData getEngineData() {
        EngineData engineData;
        try {
            EngineDataBean bean = getEngineDataBean();
            Bean2Impl b2i = new Bean2Impl();
            engineData = b2i.engineData(bean);
        } catch (Exception e) {
            engineData = new EngineData();
        }

        return engineData;
    }

    public EngineDataBean getEngineDataBean() {
        return (EngineDataBean) dao.getObject(EngineDataBean.class, "1");
    }

    public void persistDeployment(Deployment deployment) {
        DeploymentBean deploymentBean = new DeploymentBean();
        deploymentBean.setDefinitionsId(deployment.getDefinitionsId());
        deploymentBean.setExtractPath(deployment.getExtractPath());
        deploymentBean.setProcessDefinitionPath(deployment.getProcessDefinitionPath());
        deploymentBean.setDiagramPath(deployment.getDiagramPath());
        deploymentBean.setDeploymentTime(deployment.getDate());

        dao.save(deploymentBean);
        EngineDataBean engineDataBean = getEngineDataBean();
        if (engineDataBean != null) {
            engineDataBean.addDeployment(deploymentBean);
            dao.update(engineDataBean);
        } else {
            engineDataBean = new EngineDataBean();
            engineDataBean.setId("1");
            engineDataBean.addDeployment(deploymentBean);
            dao.save(engineDataBean);
        }
    }

    public void undeployProcess(String processId) {
        EngineDataBean bean = getEngineDataBean();
        bean.getDeployedProcesses().remove(processId);
        dao.save(bean);
        dao.remove(DeploymentBean.class, processId);
    }

    public void persistProcessInstance(ProcessInstance processInstance) {
        DeploymentBean deploymentBean = (DeploymentBean) dao.getObject(DeploymentBean.class, processInstance.getDefinitionsId());
        assert deploymentBean != null;
        ProcessInstanceBean processInstanceBean = new ProcessInstanceBean();
        processInstanceBean.setProcessId(processInstance.getProcessId());
        processInstanceBean.setDeployedProcess(deploymentBean);
        UserBean userBean = new UserBean();
        userBean.setUserId(processInstance.getStartUserId());
        UserBean user = (UserBean) dao.getObject(UserBean.class, processInstance.getStartUserId());
        if (user != null) {
            processInstanceBean.setStartUser(user);
        } else {
            processInstanceBean.setStartUser(userBean);
        }
        processInstanceBean.setStartTime(new Date());
        processInstanceBean.setStartEventId(processInstance.getProcessDefinition().getStartEvent().getId());
        processInstanceBean.setRunning(true);
        dao.save(processInstanceBean);
        if (user != null) {
            user.addStartedProcessInstances(processInstanceBean);
            dao.update(user);
        } else {
            userBean.addStartedProcessInstances(processInstanceBean);
            dao.save(userBean);
        }
        EngineDataBean engineDataBean = (EngineDataBean) dao.getObject(EngineDataBean.class, "1");
        if (engineDataBean != null) {
            engineDataBean.addProcessInstance(processInstanceBean);
            dao.update(engineDataBean);
        } else {
            engineDataBean = new EngineDataBean();
            engineDataBean.setId("1");
            engineDataBean.addProcessInstance(processInstanceBean);
            dao.save(engineDataBean);
        }

    }

    public String getProcessDefinition(String processId) {
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processId);
        return processInstanceBean.getDeployedProcess().getDefinitionsId();
    }

    public List<String> getCompletedTasks(String processId) {
        dao.close();
        dao = new HibernateDao();
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processId);
        if (processInstanceBean.getCompletedTasks() != null)
            return (new ArrayList(processInstanceBean.getCompletedTasks().keySet()));
        else return new ArrayList<String>();
    }

    public List<String> getRunningTasks(String processId) {
        dao.close();
        dao = new HibernateDao();
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processId);
        if (processInstanceBean.getRunningTasks() != null)
            return (new ArrayList(processInstanceBean.getRunningTasks().keySet()));
        else return new ArrayList<String>();
    }

    public void persistUserTask(UserTask userTask) {
        TaskBean userTaskBean = (TaskBean) dao.getObject(TaskBean.class, userTask.getId());
        if (userTaskBean == null) {
            userTaskBean = new TaskBean();
            userTaskBean.setTaskId(userTask.getId());
            userTaskBean.setTaskId(userTask.getId());
            userTaskBean.setActive(true);


            TUserTask task = userTask.getTTask();
            if (task.getAssignee() != null) {
                UserBean assignee = (UserBean) dao.getObject(UserBean.class, task.getAssignee());
                if (assignee != null) {
                    userTaskBean.setAssignee(assignee);
                }
            } else {
                String potentialOwner = (task.getResourceRoleArray()[0]).getResourceAssignmentExpression().getExpression().getDomNode().getChildNodes().item(0).getNodeValue();
                GroupBean potentialGroup = (GroupBean) dao.getObject(GroupBean.class, potentialOwner);
                if (potentialGroup != null) {
                    userTaskBean.setPotentialGroup(potentialGroup);
                }
            }
            userTaskBean.setFormName(task.getName());
            userTaskBean.setTaskName(task.getName());
            userTaskBean.setHasUserForm(userTask.hasInputForm());
            userTaskBean.setFromPath(task.getInputForm());
            dao.save(userTaskBean);

            ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, userTask.getProcessInstance().getProcessId());
            processInstanceBean.getRunningTasks().put(userTaskBean.getTaskId(), userTaskBean);
            dao.update(processInstanceBean);
        }
    }

    public void persistStartEvent(StartEvent startEvent) {
        TaskBean starteventbean = new TaskBean();
        starteventbean.setActive(true);
        starteventbean.setTaskId(startEvent.getId());
        starteventbean.setTaskName(startEvent.getName());
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, startEvent.getProcessInstance().getProcessId());

        starteventbean.setAssignee(processInstanceBean.getStartUser());
        starteventbean.setFormName(startEvent.getName());
        starteventbean.setFromPath(startEvent.getTStartEvent().getInputForm());
        dao.save(starteventbean);

        processInstanceBean.getRunningTasks().put(starteventbean.getTaskId(), starteventbean);
        dao.update(processInstanceBean);
    }

    public void addRunningTask(String taskId, String processInstanceId) {
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processInstanceId);
        TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
        if (task != null) {
            processInstanceBean.getRunningTasks().put(taskId, task);
            dao.update(processInstanceBean);
        }
    }

    public void removeRunningTask(String taskId, String processInstanceId) {
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processInstanceId);
        TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
        if (task != null) {
            processInstanceBean.getRunningTasks().remove(taskId);
            dao.update(processInstanceBean);
        }
    }

    public void addCompletedTask(String taskId, String processInstanceId) {
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processInstanceId);
        TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
        if (task != null) {
            processInstanceBean.getCompletedTasks().put(taskId, task);
            dao.update(processInstanceBean);
        }
    }

    public List<String> getDeploymentIds() {
        HibernateDao dao = new HibernateDao();
        if (dao.getObject(EngineDataBean.class, "1") == null) {
            return new ArrayList<String>();
        }
        EngineDataBean engineDataBean = (EngineDataBean) dao.getObject(EngineDataBean.class, "1");
        Map<String, DeploymentBean> deployedProcesses = engineDataBean.getDeployedProcesses();
        List<String> deploymentIds = new ArrayList<String>();
        for (String id : deployedProcesses.keySet()) {
            deploymentIds.add(deployedProcesses.get(id).getDefinitionsId());
        }
        return deploymentIds;
    }

    public String getPotentialGroup(String taskId) {
        TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
        return task.getPotentialGroup().getGroupId();
    }

    public void setVariables(String processInstanceId, Map<String, String> variables) {
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processInstanceId);
        processInstanceBean.getVariables().putAll(variables);
        dao.update(processInstanceBean);
    }

    public Map<String, String> getVariables(String processInstanceId) {
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processInstanceId);
        return processInstanceBean.getVariables();
    }

    public Map<String, TaskBean> getActiveTasksMap(String definitionId) {
        Iterator<TaskBean> tasks = qlManager.getActiveTasks(definitionId).iterator();
        Map<String, TaskBean> map = new HashMap<String, TaskBean>();
        while(tasks.hasNext()){
            TaskBean task = tasks.next();
            map.put(task.getTaskId(), task);
        }
        return map;
    }

    public List<TaskBean> getUnassignedTasks(String groupId, String definitionId) {
        return qlManager.getUnassignedTasks(groupId, definitionId);
    }

    public List<TaskBean> getUserTaskList(String userName, String definitionId) {
        return qlManager.getUserTaskList(userName, definitionId);
    }

    public List<ProcessInstanceBean> getCompletedProcessInstances(String definitionId) {
        return qlManager.getCompletedProcessInstances(definitionId);
    }

    public void persistEndEvent(String processInsId) {
        ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processInsId);
        processInstanceBean.setRunning(false);
        dao.update(processInstanceBean);
    }

    public void closeSession() {
        dao.close();
    }
    
}
