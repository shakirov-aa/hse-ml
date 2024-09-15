package hse.ml;

import java.util.Date;

public class PhishingEntity {
    private final String url;
    private final String targetedBrand;
    private final Date time;

    public PhishingEntity(String url, String targetedBrand, Date time) {
        this.url = url;
        this.targetedBrand = targetedBrand;
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public String getTargetedBrand() {
        return targetedBrand;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "PhishingEntity{" +
                "url='" + url + '\'' +
                ", targetedBrand='" + targetedBrand + '\'' +
                ", time=" + time +
                '}';
    }
}
