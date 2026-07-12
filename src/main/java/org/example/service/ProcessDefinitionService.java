package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.BusinessException;
import org.example.model.entity.FlowDefinition;
import org.example.model.entity.FlowDefinitionVersion;
import org.example.repository.FlowDefinitionMapper;
import org.example.repository.FlowDefinitionVersionMapper;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionService {

    private final FlowDefinitionMapper definitionMapper;
    private final FlowDefinitionVersionMapper versionMapper;
    private final RepositoryService repositoryService;

    // ==================== 流程定义 CRUD ====================

    public Page<FlowDefinition> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<FlowDefinition> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(FlowDefinition::getName, keyword)
                    .or().like(FlowDefinition::getFlowKey, keyword));
        }
        wrapper.orderByDesc(FlowDefinition::getCreateTime);
        return definitionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public FlowDefinition getById(Long id) {
        return definitionMapper.selectById(id);
    }

    public FlowDefinition getByKey(String flowKey) {
        return definitionMapper.selectOne(
                new LambdaQueryWrapper<FlowDefinition>().eq(FlowDefinition::getFlowKey, flowKey));
    }

    @Transactional
    public FlowDefinition create(FlowDefinition def) {
        // 检查 Key 唯一
        FlowDefinition exist = getByKey(def.getFlowKey());
        if (exist != null) {
            throw new BusinessException("流程标识 key 已存在: " + def.getFlowKey());
        }
        def.setStatus("DRAFT");
        def.setCurrentVersion(0);
        definitionMapper.insert(def);
        return def;
    }

    @Transactional
    public void update(Long id, FlowDefinition def) {
        FlowDefinition exist = definitionMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("流程定义不存在");
        }
        if (!"DRAFT".equals(exist.getStatus())) {
            throw new BusinessException("只有草稿状态的流程定义可以编辑");
        }
        def.setDefinitionId(id);
        definitionMapper.updateById(def);
    }

    @Transactional
    public void delete(Long id) {
        FlowDefinition def = definitionMapper.selectById(id);
        if (def == null) {
            throw new BusinessException("流程定义不存在");
        }
        definitionMapper.deleteById(id);
    }

    // ==================== 版本发布 ====================

    /**
     * 发布流程定义新版本
     * 1. 将 BPMN XML 部署到 Flowable 引擎
     * 2. 记录版本信息
     * 3. 设置为主版本
     */
    @Transactional
    public FlowDefinitionVersion publish(Long definitionId, Long userId) {
        FlowDefinition def = definitionMapper.selectById(definitionId);
        if (def == null) {
            throw new BusinessException("流程定义不存在");
        }
        if (def.getBpmnXml() == null || def.getBpmnXml().isBlank()) {
            throw new BusinessException("BPMN XML 不能为空，请先编辑流程图");
        }

        // 1. 部署到 Flowable 引擎
        Deployment deployment = repositoryService.createDeployment()
                .name(def.getName())
                .key(def.getFlowKey())
                .addString(def.getFlowKey() + ".bpmn20.xml", def.getBpmnXml())
                .deploy();

        // 查询部署后的 ProcessDefinition
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        if (procDef == null) {
            throw new BusinessException("Flowable 部署失败，未能获取流程定义");
        }

        // 2. 计算新版本号
        Integer maxVersion = versionMapper.selectMaxVersion(definitionId);
        int newVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        // 3. 将旧主版本取消标记
        LambdaQueryWrapper<FlowDefinitionVersion> oldMainWrapper =
                new LambdaQueryWrapper<FlowDefinitionVersion>()
                        .eq(FlowDefinitionVersion::getDefinitionId, definitionId)
                        .eq(FlowDefinitionVersion::getIsMain, 1);
        FlowDefinitionVersion oldMain = versionMapper.selectOne(oldMainWrapper);
        if (oldMain != null) {
            oldMain.setIsMain(0);
            versionMapper.updateById(oldMain);
        }

        // 4. 插入新版本记录
        FlowDefinitionVersion version = new FlowDefinitionVersion();
        version.setDefinitionId(definitionId);
        version.setVersion(newVersion);
        version.setBpmnXml(def.getBpmnXml());
        version.setProcessDefinitionId(procDef.getId());
        version.setDeploymentId(deployment.getId());
        version.setIsMain(1);
        version.setPublishTime(LocalDateTime.now());
        version.setPublishUserId(userId);
        versionMapper.insert(version);

        // 5. 更新定义主表
        def.setStatus("PUBLISHED");
        def.setCurrentVersion(newVersion);
        def.setLatestPublishTime(LocalDateTime.now());
        definitionMapper.updateById(def);

        log.info("流程定义发布成功: {} v{} -> processDefinitionId={}", def.getFlowKey(), newVersion, procDef.getId());
        return version;
    }

    /** 获取版本列表 */
    public List<FlowDefinitionVersion> getVersions(Long definitionId) {
        return versionMapper.selectByDefinitionId(definitionId);
    }

    // ==================== 状态控制 ====================

    /** 挂起流程定义（已发布→停用） */
    @Transactional
    public void suspend(Long definitionId) {
        FlowDefinition def = definitionMapper.selectById(definitionId);
        if (def == null || !"PUBLISHED".equals(def.getStatus())) {
            throw new BusinessException("只能停用已发布的流程定义");
        }
        // 挂起 Flowable 层面的流程定义
        repositoryService.suspendProcessDefinitionByKey(def.getFlowKey());
        def.setStatus("DISABLED");
        definitionMapper.updateById(def);
    }

    /** 激活流程定义（停用→已发布） */
    @Transactional
    public void activate(Long definitionId) {
        FlowDefinition def = definitionMapper.selectById(definitionId);
        if (def == null || !"DISABLED".equals(def.getStatus())) {
            throw new BusinessException("只能激活已停用的流程定义");
        }
        repositoryService.activateProcessDefinitionByKey(def.getFlowKey());
        def.setStatus("PUBLISHED");
        definitionMapper.updateById(def);
    }
}
