-- Set role user for p365 user --
insert into cosalon_upms.upms_user_role(user_id, role_id)
select u.id, r.id
from cosalon_upms.upms_user u
         inner join cosalon_upms.upms_role r
                    on r.id = (select id from cosalon_upms.upms_role r where r.auth_code = 'ROLE_USER')
where u.id >= 123
  and u.nickname not like '%h0%';

-- Migrate data from bhair_stylist to ims_customer_suggestion --
INSERT INTO cosalon_ims.ims_customer_suggestion(gmt_create, gmt_modified, version, customer_name, phone_number, type,
                                                referential_id)
SELECT CURRENT_TIMESTAMP(),
       CURRENT_TIMESTAMP(),
       0,
       st.display_name,
       st.phone_num,
       2,
       st.id
FROM cosalon_bhair.bhair_stylist st
ON DUPLICATE KEY UPDATE customer_name = st.display_name;

-- Migrate p365_user data --
INSERT INTO cosalon_ims.ims_customer_suggestion(gmt_create, gmt_modified, version, customer_name, phone_number, type,
                                                referential_id)
SELECT CURRENT_TIMESTAMP(),
       CURRENT_TIMESTAMP(),
       0,
       u.nickname,
       u.phone_num,
       1,
       u.id
FROM cosalon_upms.upms_user u
         JOIN cosalon_upms.upms_user_role ur on u.id = ur.user_id
WHERE u.is_sys_built_in = 0
  and u.phone_num is not null
  and lower(nickname) not like '%h0%'
  and ur.role_id = (select id from cosalon_upms.upms_role r where r.auth_code = 'ROLE_USER')
ON DUPLICATE KEY UPDATE phone_number   = u.phone_num,
                        referential_id = u.id;