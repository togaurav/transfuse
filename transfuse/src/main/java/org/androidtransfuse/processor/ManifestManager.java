package org.androidtransfuse.processor;

import org.androidtransfuse.model.manifest.*;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John Ericksen
 */
@Singleton
public class ManifestManager {

    private Application application;
    private List<Activity> activities = new ArrayList<Activity>();
    private List<Receiver> broadcastReceivers = new ArrayList<Receiver>();
    private List<Service> services = new ArrayList<Service>();

    public void setApplication(Application application) {
        this.application = application;
    }

    public void addActivity(Activity activity) {
        this.activities.add(activity);
    }

    public void addBroadcastReceiver(Receiver broadcastReceiver) {
        this.broadcastReceivers.add(broadcastReceiver);
    }

    public void addService(Service service) {
       this.services.add(service);
    }

    public Manifest getManifest() {
        Manifest manifest = new Manifest();

        Application localApplication = application;

        if (application == null) {
            localApplication = new Application();
        }

        localApplication.getActivities().addAll(activities);
        localApplication.getReceivers().addAll(broadcastReceivers);
        localApplication.getServices().addAll(services);

        manifest.getApplications().add(localApplication);

        return manifest;
    }
}