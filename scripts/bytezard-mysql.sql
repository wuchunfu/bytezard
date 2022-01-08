
CREATE TABLE `bytezard_command` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `type` tinyint(4) DEFAULT 0 COMMENT 'Command type: 0 start job, 1 start execution from current node, 2 resume fault-tolerant workflow, 3 resume pause process, 4 start execution from failed node, 5 complement, 6 schedule, 7 rerun, 8 pause, 9 stop, 10 resume waiting thread',
    `parameter` text NOT NULL COMMENT 'json command parameters',
    `project_id` bigint(20) NOT NULL COMMENT 'project id',
    `job_id` bigint(20) NOT NULL COMMENT 'job id',
    `priority` int(11) DEFAULT NULL COMMENT 'process instance priority: 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
    `schedule_time` datetime DEFAULT NULL COMMENT 'schedule time',
    `submit_time` datetime DEFAULT NULL COMMENT 'submit time',
    `start_time` datetime DEFAULT NULL COMMENT 'start time',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `bytezard_execution_job` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `job_instance_id` bigint(20) NOT NULL COMMENT 'job instance id',
    `job_name` varchar(255) DEFAULT NULL COMMENT '任务名称',
    `job_json` longtext COMMENT '任务细节，JSON格式',
    `execute_path` varchar(255) DEFAULT NULL COMMENT '本地执行地址',
    `log_path` varchar(255) DEFAULT NULL COMMENT 'Log本地存储路径',
    `result_path` varchar(255) DEFAULT NULL COMMENT '结果输出地址',
    `execute_host` varchar(255) DEFAULT NULL COMMENT '执行任务的主机地址',
    `job_unique_id` varchar(255) DEFAULT NULL COMMENT 'task 唯一标识ID',
    `process_id` int(11) DEFAULT NULL COMMENT '进程ID',
    `application_id` text COMMENT '关联的应用ID，如yarn application id',
    `job_parameters` longtext COMMENT '任务参数',
    `tenant_code` varchar(255) DEFAULT NULL COMMENT '租户',
    `env_file` varchar(255) DEFAULT NULL COMMENT '执行机器的环境文件',
    `submit_time` datetime DEFAULT NULL COMMENT '任务的提交时间',
    `start_time` datetime DEFAULT NULL COMMENT '任务开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '任务完成时间',
    `queue` varchar(255) DEFAULT '' COMMENT '运行队列',
    `status` int(11) DEFAULT NULL COMMENT '状态',
    `executor_group` varchar(255) DEFAULT NULL COMMENT '分组',
    `timeout` int(11) DEFAULT NULL COMMENT '超时时间',
    `timeout_strategy` tinyint(4) DEFAULT NULL COMMENT '超时策略',
    `retry_nums` int(11) DEFAULT NULL COMMENT '重试次数',
    `resources` longtext COMMENT '资源路径',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `bytezard_job_definition` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) DEFAULT NULL COMMENT '任务名称',
    `parameter` text COMMENT '任务参数',
    `json` text,
    `project_id` bigint(20) DEFAULT NULL COMMENT 'project id',
    `retry_times` int(11) DEFAULT NULL COMMENT '重试次数',
    `retry_interval` int(11) DEFAULT NULL COMMENT '重试间隔',
    `timeout` int(11) DEFAULT NULL COMMENT '任务超时时间',
    `timeout_strategy` int(11) DEFAULT NULL COMMENT '超时策略',
    `tenant_code` varchar(255) DEFAULT NULL COMMENT '代理用户',
    `env_file` varchar(255) DEFAULT NULL COMMENT '配置文件',
    `resources` text COMMENT '资源列表',
    `create_user_id` int(11) DEFAULT NULL COMMENT '创建用户id',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_name` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `bytezard_job_instance` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `job_definition_id` bigint(20) NOT NULL,
    `project_flow_id` bigint(20) NOT NULL,
    `parameter` text,
    `json` text,
    `status` int(11) DEFAULT NULL,
    `retry_times` int(11) DEFAULT NULL COMMENT '重试次数',
    `retry_interval` int(11) DEFAULT NULL COMMENT '重试间隔',
    `timeout` int(11) DEFAULT NULL COMMENT '超时时间',
    `timeout_strategy` int(11) DEFAULT NULL COMMENT '超时处理策略',
    `tenant_code` varchar(255) DEFAULT NULL COMMENT '代理用户',
    `env_file` varchar(255) DEFAULT NULL COMMENT '环境文件',
    `resources` text COMMENT '资源列表',
    `execution_id` int(20) DEFAULT NULL,
    `submit_time` datetime DEFAULT NULL,
    `start_time` datetime DEFAULT NULL,
    `end_time` datetime DEFAULT NULL,
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `bytezard_project` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) DEFAULT NULL COMMENT '项目名称',
    `description` varchar(255) DEFAULT NULL COMMENT '项目描述',
    `create_user_id` int(11) DEFAULT NULL COMMENT '创建用户ID',
    `version` varchar(255) DEFAULT NULL,
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;