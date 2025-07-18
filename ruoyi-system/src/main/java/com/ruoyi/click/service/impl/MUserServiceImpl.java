package com.ruoyi.click.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.click.mapper.UserGradeMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.*;
import com.ruoyi.common.core.domain.entity.MUser;
import com.ruoyi.click.domain.MAccountChangeRecords;
import com.ruoyi.click.domain.UserGrade;
import com.ruoyi.click.domain.vo.UserRegisterModel;
import com.ruoyi.click.domain.vo.BalanceModel;
import com.ruoyi.click.mapper.MUserMapper;
import com.ruoyi.click.service.IMAccountChangeRecordsService;
import com.ruoyi.click.service.IMUserService;
import com.ruoyi.click.service.IUserGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户Service业务层处理
 *
 * @author ruoyi
 * @date 2025-06-15
 */
@Service
public class MUserServiceImpl extends ServiceImpl<MUserMapper, MUser>  implements IMUserService {
    @Autowired
    private MUserMapper mUserMapper;

    @Autowired
    private IUserGradeService userGradeService;
    @Autowired
    private UserGradeMapper userGradeMapper;
    @Autowired
    private IMAccountChangeRecordsService  accountChangeRecordsService;

    /**
     * 查询用户
     *
     * @param uid 用户主键
     * @return 用户
     */
    @Override
    public MUser selectMUserByUid(Long uid)
    {
        return mUserMapper.selectMUserByUid(uid);
    }

    /**
     * 查询用户列表
     *
     * @param mUser 用户
     * @return 用户
     */
    @Override
    public List<MUser> selectMUserList(MUser mUser)
    {
        return mUserMapper.selectMUserList(mUser);
    }
    /**
     * 新增用户
     *
     * @param mUser 用户
     * @return 结果
     */
    @Override
    public int insertMUser(MUser mUser) {
        if (StringUtils.isNotBlank(mUser.getInviterCode())) {
            MUser inviterUser = this.getOne(
                    new LambdaQueryWrapper<MUser>()
                            .eq(MUser::getInvitationCode, mUser.getInviterCode())
            );

            if (inviterUser == null) {
                throw new ServiceException("邀请码无效");
            }

            mUser.setInviter(String.valueOf(inviterUser.getUid()));
            mUser.setInviterName(inviterUser.getLoginAccount());
            mUser.setInviterCode(inviterUser.getInvitationCode());


        }

        // 校验账号是否重复
        String loginAccount = mUser.getLoginAccount();
        if (StringUtils.isNotBlank(loginAccount) &&
                this.getOne(new LambdaQueryWrapper<MUser>().eq(MUser::getLoginAccount, loginAccount)) != null) {
            throw new ServiceException("账号已存在");
        }

        mUser.setBrushNumber(0);
        mUser.setAccountBalance(DecimalUtil.toBigDecimal(0));
        mUser.setLoginPassword(EncoderUtil.encoder(mUser.getLoginPassword()));
        mUser.setFundPassword(EncoderUtil.encoder(mUser.getFundPassword()));
        mUser.setInvitationCode(generateCode());
        mUser.setCreateTime(DateUtils.getNowDate());
        mUser.setStatus(1);
        return mUserMapper.insertMUser(mUser);
    }


    public  String generateCode() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            char letter = (char) ('A' + random.nextInt(26));
            sb.append(letter);
        }
        long count = this.count(new LambdaQueryWrapper<MUser>().eq(MUser::getInvitationCode, sb.toString()));
        if(count>0){
            generateCode();
        }
        return sb.toString();
    }

    /**
     * 修改用户
     *
     * @param mUser 用户
     * @return 结果
     */
    @Override
    public int updateMUser(MUser mUser)
    {
        mUser.setUpdateTime(DateUtils.getNowDate());

        MUser user = mUserMapper.selectMUserByUid(mUser.getUid());
        if(!user.getLoginPassword().equals(mUser.getLoginPassword())){
            mUser.setLoginPassword(EncoderUtil.encoder(mUser.getLoginPassword()));

        }
        if(!user.getFundPassword().equals(mUser.getFundPassword())){
            mUser.setFundPassword(EncoderUtil.encoder(mUser.getFundPassword()));

        }
        if(mUser.getInviterCode()!=null){
            if(!user.getInviterCode().equals(mUser.getInviterCode())){
                MUser one = this.getOne(new LambdaQueryWrapper<MUser>()
                        .eq(MUser::getInvitationCode, mUser.getInviterCode()));
                mUser.setInviter(String.valueOf(one.getUid()));
                mUser.setInviterName(one.getLoginAccount());
                mUser.setInviterCode(one.getInvitationCode());
            }
        }

        return mUserMapper.updateMUser(mUser);
    }

    /**
     * 修改用户，用于前端修改银行信息
     *
     * @param mUser 用户
     * @return 结果
     */
    @Override
    public int updateUserBank(MUser mUser)
    {
        mUser.setUpdateTime(DateUtils.getNowDate());
        MUser user = mUserMapper.selectMUserByUid(mUser.getUid());

        if(!user.getFundPassword().equals(mUser.getFundPassword())){
            mUser.setFundPassword(EncoderUtil.encoder(mUser.getFundPassword()));
        }

        return mUserMapper.updateMUser(mUser);
    }

    /**
     * 批量删除用户
     *
     * @param uids 需要删除的用户主键
     * @return 结果
     */
    @Override
    public int deleteMUserByUids(Long[] uids)
    {
        return mUserMapper.deleteMUserByUids(uids);
    }

    /**
     * 删除用户信息
     *
     * @param uid 用户主键
     * @return 结果
     */
    @Override
    public int deleteMUserByUid(Long uid)
    {
        return mUserMapper.deleteMUserByUid(uid);
    }

    @Override
    public void userRegister(UserRegisterModel model) {
        MUser inviterUser = this.getOne(
                new LambdaQueryWrapper<MUser>()
                        .eq(MUser::getInvitationCode, model.getInvitationCode())
        );

        if (inviterUser == null) {
            throw new ServiceException("邀请码无效");
        }

        MUser mUser = new MUser();
        mUser.setInviter(String.valueOf(inviterUser.getUid()));
        mUser.setInviterName(inviterUser.getLoginAccount());
        mUser.setInviterCode(inviterUser.getInvitationCode());

        // 校验账号是否重复
        String loginAccount = model.getLoginAccount();
        if (StringUtils.isNotBlank(loginAccount) &&
                this.getOne(new LambdaQueryWrapper<MUser>().eq(MUser::getLoginAccount, loginAccount)) != null) {
            throw new ServiceException("账号已存在");
        }
        mUser.setLoginAccount(loginAccount);
        mUser.setBrushNumber(0);
        mUser.setAccountBalance(DecimalUtil.toBigDecimal(0));
        mUser.setLoginPassword(EncoderUtil.encoder(model.getLoginPassword()));
        mUser.setFundPassword(EncoderUtil.encoder(model.getFundPassword()));
        mUser.setInvitationCode(generateCode());
        mUser.setCreateTime(DateUtils.getNowDate());
        mUser.setRegisterType("1");
        mUser.setPhoneNumber(model.getPhone());
        mUser.setStatus(1);
        // TODO 此处需要思考
        mUser.setLevel(1);
//        UserGrade minGrade = userGradeService.getOne(
//                new LambdaQueryWrapper<UserGrade>()
//                        .orderByAsc(UserGrade::getSortNum)
//                        .last("LIMIT 1")
//        );
        mUserMapper.insertMUser(mUser);
    }

    @Override
    public MUser getUserOne(String loginAccount, String loginPassword) {
        LambdaQueryWrapper<MUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MUser::getLoginAccount, loginAccount);
        MUser user = this.getOne(wrapper);
        if (user == null) {
            throw new ServiceException("没有该用户");
        }

        boolean matches = EncoderUtil.matches(loginPassword, user.getLoginPassword());
        if (!matches) {
            throw new ServiceException("密码错误");
        }
        return user;
    }

    @Override
    public HashMap<String, Object> updateBalance(MUser mUser, BalanceModel balanceModel) {
        if(mUser==null){
            throw new ServiceException("用户已删除");
        }
        BigDecimal accountBalance = mUser.getAccountBalance();
        BigDecimal changeBalance = balanceModel.getBalance();
        Integer type = 0;
        if(changeBalance.signum()>=0){ //changeBalance大于或等于0
            type = 0;
        }else {
            if (accountBalance.compareTo(changeBalance) < 0) {
                throw new ServiceException("当前账户金额小于减少金额");
            }
            type = 1;
        }
        accountBalance = DecimalUtil.add(accountBalance, changeBalance);
        mUser.setAccountBalance(accountBalance);
        this.updateMUser(mUser);

        HashMap<String, Object> map = new HashMap<>();
        map.put("accountBalance",accountBalance);
        map.put("type",type);
        return map;
    }


    @Override
    public void upgrade(Long uid) {
        MUser mUser = this.selectMUserByUid(uid);
        if (mUser == null) return;

        // 获取用户充值总额
        MAccountChangeRecords changeRecords = new MAccountChangeRecords();
        changeRecords.setUid(String.valueOf(uid));
        changeRecords.setType(0); // 增加
        changeRecords.setTransactionType(1); // 充值

        List<MAccountChangeRecords> selected = accountChangeRecordsService.selectMAccountChangeRecordsList(changeRecords);
        BigDecimal totalAmount = selected.stream()
                .map(MAccountChangeRecords::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 查询所有等级中 join_cost <= totalAmount 的，按 sort_num 降序取第一个
        List<UserGrade> matchedGrades = userGradeService.list(
                new LambdaQueryWrapper<UserGrade>()
                        .apply("CAST(join_cost AS DECIMAL(20,6)) <= {0}", totalAmount)
                        .orderByDesc(UserGrade::getSortNum)
        );

        if (!matchedGrades.isEmpty()) {
            UserGrade targetGrade = matchedGrades.get(0);
            int currentLevel = mUser.getLevel();
            if (targetGrade.getSortNum() > currentLevel) {
                // 升级
                mUser.setLevel(targetGrade.getSortNum().intValue());
                this.updateMUser(mUser);
            }
        }
    }



    /**
     * 用户当天刷单数量清零
     */
    @Override
    public void clearBrushNumber(){
        mUserMapper.clearBrushNumber();
    }

    /**
     * 用户当日订单数量加1
     * @param uid 用户主键
     * @return
     */
    @Override
    public int increaseBrushNumber(Long uid, Integer num){
        return mUserMapper.increaseBrushNumber(uid, num);
    }



    /**
     * 直接修改用户信息，不涉及其他业务
     *
     * @param mUser 用户
     * @return 结果
     */
    @Override
    public int updateMUserSimple(MUser mUser)
    {
        mUser.setUpdateTime(DateUtils.getNowDate());
        return mUserMapper.updateMUser(mUser);
    }

    /**
     * 修改用户连单数量
     *
     * @param mUser 用户
     * @return 结果
     */
    @Override
    public int updateMultiOrderNum(MUser mUser)
    {
        mUser.setUpdateTime(DateUtils.getNowDate());
        return mUserMapper.updateMUser(mUser);
    }


    /**
     * 修改当前用户的等级
     *
     * @param gradeId 用户等级id
     * @param userId 用户id
     * @return 结果
     */
    @Override
    public int updateGrade(Long gradeId, Long userId)
    {
        UserGrade userGrade = userGradeMapper.selectUserGradeById(gradeId);
        BigDecimal minBalance = userGrade.getMinBalance();
        MUser user = mUserMapper.selectMUserByUid(userId);
        if(userGrade.getSortNum() < user.getLevel())
            throw new ServiceException("您无法升级到低于当前级别的级别");
        if(user.getAccountBalance().compareTo(minBalance) < 0)
            throw new ServiceException("余额不足无法升级");

        user.setLevel(userGrade.getSortNum());
        user.setUpdateTime(DateUtils.getNowDate());
        return mUserMapper.updateMUser(user);
    }

}
