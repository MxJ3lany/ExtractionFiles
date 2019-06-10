package com.bitlove.fetlife.event;

import com.bitlove.fetlife.model.service.FetLifeApiIntentService;

public class PictureUploadStartedEvent extends ServiceCallStartedEvent {

    private String pictureId;

    public PictureUploadStartedEvent(String pictureId) {
        super(FetLifeApiIntentService.ACTION_APICALL_UPLOAD_PICTURE);
        this.pictureId = pictureId;
    }

    public String getPictureId() {
        return pictureId;
    }

}
