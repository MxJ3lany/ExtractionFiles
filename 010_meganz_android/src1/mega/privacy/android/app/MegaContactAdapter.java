package mega.privacy.android.app;


import nz.mega.sdk.MegaUser;

public class MegaContactAdapter {

    MegaContactDB megaContactDB;
    MegaUser megaUser;
    String fullName;

    String lastGreen;

    public MegaContactAdapter(MegaContactDB megaContactDB, MegaUser megaUser, String fullName) {
        this.megaContactDB = megaContactDB;
        this.megaUser = megaUser;
        this.fullName = fullName;
        this.lastGreen = "";
    }

    public MegaContactDB getMegaContactDB() {
        return megaContactDB;
    }

    public void setMegaContactDB(MegaContactDB megaContactDB) {
        this.megaContactDB = megaContactDB;
    }

    public MegaUser getMegaUser() {
        return megaUser;
    }

    public void setMegaUser(MegaUser megaUser) {
        this.megaUser = megaUser;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLastGreen() {
        return lastGreen;
    }

    public void setLastGreen(String lastGreen) {
        this.lastGreen = lastGreen;
    }

}
