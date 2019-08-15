package io.chubao.joyqueue.model.domain;

/**
 * Created by yangyang115 on 18-9-11.
 */
public class ApplicationUser extends BaseModel {

    private Identity application;

    private Identity user;

    public ApplicationUser() {
    }

    public ApplicationUser(Identity application, Identity user) {
        this.application = application;
        this.user = user;
    }

    public Identity getApplication() {
        return application;
    }

    public void setApplication(Identity application) {
        this.application = application;
    }

    public Identity getUser() {
        return user;
    }

    public void setUser(Identity user) {
        this.user = user;
    }
}
