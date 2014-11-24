package com.hanyanan.tools.schedule.http.cache;

/**
 * Created by hanyanan on 2014/11/15.
 */
@Deprecated
public class StrictModeConflict implements OnConflict {
    @Override
    public ConflictAction onConflict(HeadConflict headConflict, ContentConflict contentConflict) {
        if(contentConflict == ContentConflict.CONTENT_MISSING){

        }



        return null;
    }
}
