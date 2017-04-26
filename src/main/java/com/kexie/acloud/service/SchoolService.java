package com.kexie.acloud.service;

import com.kexie.acloud.dao.SchoolDao;
import com.kexie.acloud.domain.School;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zojian on 2017/4/26.
 */
@Service
public class SchoolService implements ISchoolService {

    @Autowired
    private SchoolDao schoolDao;

    @Override
    public List<School> getAllSchool() {
       return schoolDao.getAllSchool();
    }

    @Override
    public School getSchoolById(int id) {
        return schoolDao.getSchoolById(id);
    }

    @Override
    public School getSchoolByName(String name) {
        return schoolDao.getSchoolByName(name);
    }

    @Override
    public boolean SchoolExists(String name) {
        return false;
    }

    @Override
    public void addSchool(School school) {
        schoolDao.addSchool(school);
    }

    @Override
    public void deleteSchool(int id) {

    }
}