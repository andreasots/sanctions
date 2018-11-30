DROP INDEX quartz.idx_ft_tg;
DROP INDEX quartz.idx_ft_t_g;
DROP INDEX quartz.idx_ft_jg;
DROP INDEX quartz.idx_ft_j_g;
DROP INDEX quartz.idx_ft_inst_job_req_rcvry;
DROP INDEX quartz.idx_ft_trig_inst_name;

DROP INDEX quartz.idx_t_nft_st_misfire_grp;
DROP INDEX quartz.idx_t_nft_st_misfire;
DROP INDEX quartz.idx_t_nft_misfire;
DROP INDEX quartz.idx_t_nft_st;
DROP INDEX quartz.idx_t_next_fire_time;
DROP INDEX quartz.idx_t_n_g_state;
DROP INDEX quartz.idx_t_n_state;
DROP INDEX quartz.idx_t_state;
DROP INDEX quartz.idx_t_g;
DROP INDEX quartz.idx_t_c;
DROP INDEX quartz.idx_t_jg;
DROP INDEX quartz.idx_t_j;

DROP INDEX quartz.idx_j_grp;
DROP INDEX quartz.idx_j_req_recovery;

DROP TABLE quartz.locks;
DROP TABLE quartz.scheduler_state;
DROP TABLE quartz.fired_triggers;
DROP TABLE quartz.paused_trigger_grps;
DROP TABLE quartz.calendars;
DROP TABLE quartz.blob_triggers;
DROP TABLE quartz.simprop_triggers;
DROP TABLE quartz.cron_triggers;
DROP TABLE quartz.simple_triggers;
DROP TABLE quartz.triggers;
DROP TABLE quartz.job_details;

DROP SCHEMA quartz;
