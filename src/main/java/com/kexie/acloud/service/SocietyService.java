package com.kexie.acloud.service;

import com.kexie.acloud.dao.ISocietyDao;
import com.kexie.acloud.dao.IUserDao;
import com.kexie.acloud.domain.Society;
import com.kexie.acloud.domain.SocietyApply;
import com.kexie.acloud.domain.SocietyPosition;
import com.kexie.acloud.domain.User;
import com.kexie.acloud.exception.AuthorizedException;
import com.kexie.acloud.exception.SocietyException;
import com.kexie.acloud.exception.UserException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created : wen
 * DateTime : 2017/5/9 23:47
 * Description :
 */
@Service
@Transactional
public class SocietyService implements ISocietyService {

    @Autowired
    private ISocietyDao mSocietyDao;

    @Autowired
    private IUserDao mUserDao;

    @Override
    public void add(Society society) throws SocietyException {
        // 学院是否有同名
        if (!mSocietyDao.hasSociety(society.getName(), society.getCollege().getId()))
            mSocietyDao.add(society);
        else
            throw new SocietyException("社团已经存在啦");
    }

    @Override
    public void updateSociety(Society society) {
        mSocietyDao.update(society);
    }

    @Override
    public Society getSocietyById(int society_id) throws SocietyException {
        Society society = mSocietyDao.getSocietyById(society_id);
        if (society == null)
            throw new SocietyException("社团不存在");
        return society;
    }

    @Override
    public List<User> getUsersIn(int society_id) {
        return mUserDao.getUserBySociety(society_id);
    }

    @Override
    public List<Society> getSoicetiesBySchoolId(int schoolId) {
        return mSocietyDao.getSocietiesBySchoolId(schoolId);
    }

    @Override
    public List<Society> getSoicetiesByCollegeId(int collegeId) {
        return mSocietyDao.getSocietiesByCollegeId(collegeId);
    }

    @Override
    public List<Society> getSocietiesByUserId(String userId) {
        return mUserDao.getSocietiesByUserId(userId);
    }

    @Override
    public void updateSocietyLogo(int societyId, String relativePath) {
        Society society = new Society();
        society.setId(societyId);
        society.setSocietyLogo(relativePath);
        mSocietyDao.update(society);
    }

    @Override
    public void changePrincipal(String oldUserId, String newUserId, int societyId) throws UserException {

        if (mUserDao.getUser(newUserId) == null)
            throw new UserException("新负责人不存在");

        List<Society> societies = mUserDao.getSocietiesByUserId(newUserId);

        boolean exist = false;
        for (Society society : societies) {
            if (societyId == society.getId()) {
                exist = true;
                break;
            }
        }
        if (!exist)
            throw new UserException("新负责人不存在这个社团中");

        Society society = new Society();
        User newPrincipal = new User();

        newPrincipal.setUserId(newUserId);
        society.setId(societyId);
        society.setPrincipal(newPrincipal);
        mSocietyDao.update(society);
    }

    @Override
    public List<Society> searchSocietyByName(String query) {
        return mSocietyDao.getSocietiesByName(query);
    }

    /**
     * 添加社团
     *
     * @param apply
     */
    @Override
    public void applyJoinSociety(SocietyApply apply) throws SocietyException {

        if (!mSocietyDao.hasSociety(apply.getSociety().getId()))
            throw new SocietyException("社团不存在");

        // 添加一条申请记录
        mSocietyDao.addApply(apply);
    }

    /**
     * 获取社团所有的申请请求
     *
     * @param societyId
     * @param userId
     * @return
     */
    @Override
    public List<SocietyApply> getSocietyApplyIn(String societyId, String userId) throws SocietyException, AuthorizedException {

        SocietyPosition position = mSocietyDao.getSocietyPositionByUserId(userId, new Integer(societyId));

        if (position == null)
            throw new SocietyException("用户社团职位为空");

        if (!position.getName().contains("主席"))
            throw new AuthorizedException("用户没有权限查询 社团申请(职位没有包含主席");

        if (!mSocietyDao.hasSociety(new Integer(societyId)))
            throw new SocietyException("社团不存在");

        return mSocietyDao.getAllSocietyApply(new Integer(societyId));
    }

    @Override
    public void handleSocietyApple(String applyId, boolean isAllow, String userId) throws SocietyException, AuthorizedException {

        SocietyApply societyApply = mSocietyDao.getSocietyApply(new Integer(applyId));

        if (societyApply == null)
            throw new SocietyException("当前申请 不存在");

        SocietyPosition position = mSocietyDao.getSocietyPositionByUserId(userId, societyApply.getSociety().getId());

        if (position == null)
            throw new SocietyException("用户社团职位为空");

        if (!position.getName().contains("主席"))
            throw new AuthorizedException("用户没有权限查询 社团申请(职位没有包含主席");
        if (isAllow) {
            mSocietyDao.addMember(societyApply.getSociety().getId(), societyApply.getUser().getUserId());
            // TODO: 2017/5/30 推送到用户中
        } else {
            // TODO: 2017/5/30 推送到用户中
        }

        // 删除这条申请
        mSocietyDao.deleteSocietyApply(new Integer(applyId));
    }

    @Override
    public void quitSociety(String societyId, String userId) {

    }
}