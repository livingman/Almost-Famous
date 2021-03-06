package com.noseparte.battle.match;

import LockstepProto.C2SMatchCancel;
import com.noseparte.battle.server.Protocol;
import com.noseparte.common.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMatchCancel extends Protocol {
    @Override
    protected void process() throws Exception {
        C2SMatchCancel req = C2SMatchCancel.parseFrom(msg);
        MatchMgr matchMgr = SpringContextUtils.getBean("matchMgr", MatchMgr.class);
        matchMgr.remove(getSid(), MatchMgr.MatchEnum.CANCEL);
    }
}
