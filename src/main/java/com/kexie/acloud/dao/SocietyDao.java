package com.kexie.acloud.dao;

import com.kexie.acloud.domain.*;
import com.kexie.acloud.util.BeanUtil;
import com.kexie.acloud.util.MyJedisConnectionFactory;
import com.kexie.acloud.util.RedisUtil;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import java.util.List;

/**
 * Created : wen
 * DateTime : 2017/4/24 21:40
 * Description :
 */
@Repository
public class SocietyDao extends HibernateDaoSupport implements ISocietyDao {

    @Autowired
    MyJedisConnectionFactory jedisConnectionFactory;

    @Autowired
    TaskExecutor taskExecutor;

    @Resource
    public void setSuperSessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    @Override
    public Society getSocietyById(int society_id) {
        return getHibernateTemplate().get(Society.class, society_id);
    }

    @Override
    public List<Society> getSocietiesBySchoolId(int schoolId) {
        return (List<Society>) getHibernateTemplate()
                .find("from Society where college.school.id = ?", schoolId);
    }

    @Override
    public List<Society> getSocietiesByCollegeId(int collegeId) {
        return (List<Society>) getHibernateTemplate().find("from Society where college.id = ?", collegeId);
    }

    @Override
    public void update(Society society) {
        Society s = getHibernateTemplate().get(Society.class, society.getId());
        BeanUtil.copyProperties(society, s);
        getHibernateTemplate().update(s);
    }

    @Override
    public List<Society> getSocietiesByName(String query) {
        return (List<Society>) getHibernateTemplate().find("from Society  where name like ?", "%" + query + "%");
    }

    @Override
    public SocietyPosition getSocietyPositionByUserId(String userId, int societyId) {
        User u = getHibernateTemplate().get(User.class, userId);
        for (SocietyPosition position : u.getSocietyPositions()) {
            if (position.getSociety().getId() == societyId)
                return position;
        }
        return null;
    }

    @Override
    public boolean isInSociety(int societyId, String userId) {
        User u = getHibernateTemplate().get(User.class, userId);
        for (SocietyPosition position : u.getSocietyPositions()) {
            if (position.getSociety().getId() == societyId)
                return true;
        }
        return false;
    }

    @Override
    public boolean isInSociety(int societyId, List<User> users) {
        if (users == null)
            return false;
        for (User user : users) {
            if (!isInSociety(societyId, user.getUserId()))
                return false;
        }
        return true;
    }

    @Override
    public boolean hasSociety(String societyName, int collegeId) {
        return getHibernateTemplate()
                .find("from Society where name = ? and college.id = ?",
                        societyName, collegeId)
                .size() > 0;
    }

    @Override
    public boolean hasSociety(int societyId) {
        return getHibernateTemplate().get(Society.class, societyId) != null;
    }

    @Override
    public void add(Society society) {
        getHibernateTemplate().save(society);
    }

    /**
     * 添加成员
     *
     * @param position
     * @param userId
     */
    @Override
    public void addMember(SocietyPosition position, String userId) {
        User user = getHibernateTemplate().load(User.class, userId);
        user.getSocietyPositions().add(position);
    }

    @Override
    public void addApply(SocietyApply apply) {
        // 添加一个加入社团的申请
        getHibernateTemplate().save(apply);
    }

    @Override
    public List<SocietyApply> getAllSocietyApply(Integer societyId) {
        return (List<SocietyApply>) getHibernateTemplate().find("from society_apply where society_id = ?", societyId);
    }

    /**
     * 根据ID获取社团申请
     *
     * @param societyApplyId
     * @return
     */
    @Override
    public SocietyApply getSocietyApplyById(int societyApplyId, String userId, String identifier) {
        if (identifier != null) {
            RedisUtil.deleteMsg(jedisConnectionFactory.getJedis(),
                    userId,
                    identifier,
                    "apply");
        }
        return getHibernateTemplate().get(SocietyApply.class, societyApplyId);
    }


    @Override
    public SocietyApply getSocietyApply(int applyId) {
        return getHibernateTemplate().get(SocietyApply.class, applyId);
    }

    @Override
    public void deleteSocietyApply(int applyId) {
        getHibernateTemplate().flush();
        getHibernateTemplate().clear();
        SocietyApply societyApply = new SocietyApply();
        societyApply.setId(applyId);
        getHibernateTemplate().delete(societyApply);
    }

    @Override
    public SocietyPosition getLowestPosition(Society society) {
        return (SocietyPosition) getHibernateTemplate()
                .find("from society_position sp where sp.society = ? and sp.grade = 1", society)
                .get(0);
    }

    @Override
    public List<SocietyPosition> getSocietyPosition(int societyId) {
        return (List<SocietyPosition>) getHibernateTemplate()
                .find("from society_position  where society_id = ?", societyId);
    }

    @Override
    public void deleteMember(int societyId, String userId) {
        User user = getHibernateTemplate().load(User.class, userId);
        List<SocietyPosition> positions = user.getSocietyPositions();
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i).getSociety().getId() == societyId) {
                positions.remove(i);
                break;
            }
        }
        user.setSocietyPositions(positions);
        getHibernateTemplate().update(user);
    }

    public List<SocietyApply> getApplyByUserIdAndSocietyId(String userId, int societyId) {
        return (List<SocietyApply>) getHibernateTemplate().find("from society_apply where user_id=? and society_id=?", userId, societyId);
    }

    /**
     * 根据职位ID获取职位信息
     *
     * @param positionId
     * @return
     */
    @Override
    public SocietyPosition getPositionByPositionId(int positionId) {
        return getHibernateTemplate().get(SocietyPosition.class, positionId);
    }

    /**
     * 向社团中添加一个职位
     *
     * @param society  社团
     * @param position 社团职位
     */
    @Override
    public void addPosition(Society society, SocietyPosition position) {
        position.setSociety(society);
        getHibernateTemplate().save(position);
    }

    /**
     * 添加一个邀请
     *
     * @param invitation
     */
    @Override
    public void addInvitation(SocietyInvitation invitation) {
        getHibernateTemplate().save(invitation);
    }

    /**
     * 判断数据库中是否有相同的申请记录
     * 既一个社团是否邀请了这个用户
     *
     * @param invitation
     */
    @Override
    public boolean hasInvitation(SocietyInvitation invitation) {
        return getHibernateTemplate()
                // TODO: 2017/6/10 不是社团已经邀请过这个人，是邀请者是否重复邀请了
                .find("from society_invitation where society.id =? and invitaUser.userId = ?",
                        invitation.getSociety().getId(), invitation.getInvitaUser().getUserId())
                .size() != 0;
    }

    @Override
    public boolean hasInvitation(int inviteId) {
        return getHibernateTemplate().get(SocietyInvitation.class, inviteId) != null;
    }

    @Override
    public SocietyInvitation getInvitation(int inviteId) {
        return getHibernateTemplate().get(SocietyInvitation.class, inviteId);
    }

    /**
     * 删除一条邀请
     *
     * @param invitationId
     */
    @Override
    public void deleteInvitation(int invitationId) {
        getHibernateTemplate().flush();
        getHibernateTemplate().clear();
        SocietyInvitation invitation = new SocietyInvitation();
        invitation.setInvitationId(invitationId);
        getHibernateTemplate().delete(invitation);
    }

    /**
     * 获取用户的社团邀请
     *
     * @param userId
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<SocietyInvitation> getInvitationByUserId(String userId) {
        return (List<SocietyInvitation>) getHibernateTemplate()
                .find("from society_invitation where invitaUser.userId = ?", userId);
    }
}
