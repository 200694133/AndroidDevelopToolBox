package com.hanyanan.tools.schedule.http.cache;

/**
 * Created by hanyanan on 2014/11/15.
 */
@Deprecated
public interface OnConflict {
    public static class ConflictAction{
        public static final int ACTION_HEAD_REMOVE_MASK = 0x01;
        public static final int ACTION_HEAD_UPDATE_MASK = 0x02;
        public static final int ACTION_HEAD_IGNORE_MASK = 0x04;
        public static final int ACTION_CONTENT_REMOVE_MASK = 0x01;
        public static final int ACTION_CONTENT_UPDATE_MASK = 0x02;
        public static final int ACTION_CONTENT_IGNORE_MASK = 0x04;
        public int headAction;
        public int contentAction;
    }

    public static enum HeadConflict{
        HEAD_MISSING,
        HEAD_EXPIRE,
        NORMAL
    }

    public static enum ContentConflict{
        NORMAL, CONTENT_MISSING
    }

    public ConflictAction onConflict(HeadConflict headConflict, ContentConflict contentConflict);
}
