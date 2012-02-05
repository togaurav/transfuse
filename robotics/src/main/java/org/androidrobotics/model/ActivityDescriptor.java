package org.androidrobotics.model;

import org.androidrobotics.model.manifest.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Ericksen
 */
public class ActivityDescriptor {

    private PackageClass packageClass;
    private int layout;
    private List<InjectionNode> injectionNodes = new ArrayList<InjectionNode>();
    private String label;
    private Activity manifestActivity;

    public PackageClass getPackageClass() {
        return packageClass;
    }

    public void setPackageClass(PackageClass packageClass) {
        this.packageClass = packageClass;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public void addInjectionNode(InjectionNode injectionNode) {
        injectionNodes.add(injectionNode);
    }

    public List<InjectionNode> getInjectionNodes() {
        return injectionNodes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Activity getManifestActivity() {
        return manifestActivity;
    }

    public void setManifestActivity(Activity manifestActivity) {
        this.manifestActivity = manifestActivity;
    }
}
