package com.bitlove.fetlife.event;

import com.bitlove.fetlife.model.service.FetLifeApiIntentService;

public class PictureUploadFailedEvent extends ServiceCallFailedEvent {

    private String pictureId;

    public PictureUploadFailedEvent(String pictureId, boolean serverConnectionFailed) {
        super(FetLifeApiIntentService.ACTION_APICALL_UPLOAD_PICTURE, serverConnectionFailed);
        this.pictureId = pictureId;
    }

    public String getPictureId() {
        return pictureId;
    }

}
