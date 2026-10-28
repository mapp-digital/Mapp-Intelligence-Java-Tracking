# Campaign

> Based on the public documentation <https://docs.mapp.com/docs/campaign-data-object-java-library-s2s> (retrieved 2026-09-29), updated to the current code of this repository.

Campaign tracking in Mapp Intelligence enables you to analyze how visitors reach your website — whether through paid advertising, newsletters, or other marketing activities.

In a broader sense, campaigns represent all entry sources that bring users to your site.

However, this section focuses on campaigns that can be technically tracked, meaning those that include a media code (a specific URL parameter used to identify the campaign source).

Typical examples include:

- Email newsletters (wt_mc=newsletter.2025.spring)
- Google Ads or display banners (mc=display.30off.blue)
- Social media promotions or affiliate links (utm_source=facebook&utm_campaign=summer_sale)

Before any campaign data can be tracked, a campaign setup must exist in Mapp Q3 under *Configuration > Marketing Configuration*. This setup defines how media codes, ad media, and campaign parameters are structured and recognized in Mapp Intelligence. Without this configuration, no campaign information is collected, even if URLs contain tracking parameters. For more background on campaign concepts, see [Campaign Configuration](https://docs.mapp.com/docs/analyses-31-campaign-configuration) in the documentation.

---

## **Campaign ID**

The campaign ID uniquely identifies each campaign tracked in Mapp Intelligence.

It consists of the **media code name** and its **value**, separated by "%3D" (URL-encoded =).

**Example:**

```text
wt_mc%3Dnewsletter.2025.spring
```

This ID format ensures consistent attribution between the tracked visit and the campaign setup in Mapp Intelligence.

Several campaign IDs are joined with `;` (e.g. `mc%3Dbanner.top;wt_mc%3Dnewsletter.2025.spring`).

The campaign ID is determined as follows:

1. An ID set explicitly with the constructor or `setId` is always used.
2. Without an explicit ID, the library reads the campaign from the request URL configured with `setRequestURL` (see [Integration and Configuration](../integration-and-configuration.md)): for every media code name (see below) that has a non-empty value in the URL, `<name>%3D<value>` is added, in the order of the media code list.
3. If neither yields an ID, no campaign ID is sent.

---

## **Media Code**

The media code defines the URL parameter used for campaign identification, such as mc, wt_mc, or utm_campaign.

Set the media code names with `setMediaCode`. The default is `mc` and `wt_mc`. The library uses them to read the campaign ID from the request URL when no ID was set explicitly.

Specifying the correct parameter name ensures that campaign entries are correctly attributed to their sources. Without this configuration, campaign tracking accuracy may be reduced — for example, if firewalls or browsers remove unknown parameters. This configuration only needs to be implemented on **target or landing pages** that serve as campaign entry points.

---

## **Once per Session**

With `setOncePerSession(true)`, a campaign is transmitted only once per session:

- The campaigns already transmitted are read from the cookie `wtstp_tcc` of the incoming request (pass the client cookies with `setCookie` / `addCookie`, see [Integration and Configuration](../integration-and-configuration.md)).
- The check works per campaign: already transmitted campaigns are dropped from the ID, new ones are still sent and remembered.
- If no new campaign remains, the campaign ID `ignore%3Dignore` is sent instead.
- This also applies to an ID set explicitly with `setId`.

You have to return the updated list of transmitted campaigns to the browser: `MappIntelligenceTracking.getTransmittedCampaignsCookie()` returns a `MappIntelligenceCookie` (see [Interfaces](../interfaces.md)) that you add to your HTTP response. It returns `null` if the list did not change. The cookie is a session cookie (max age `-1`, path `/`), is not HttpOnly (the pixel reads it as well), and is Secure unless the request URL uses plain `http`. Without this cookie, once per session cannot work across requests.

---

## **Parameters**

Campaign parameters provide additional details about an advertising medium, such as:

- The position of a clicked link within a newsletter
- The banner variant in a display campaign
- The creative or language version of an ad

They can be defined either in the campaign configuration or directly in the page configuration.

If the same parameter is defined in both places, the page configuration takes precedence and overwrites the URL parameter.

> [!NOTE]
> The library reads only the media codes from the request URL. Campaign parameters contained in the URL (such as `wt_cc1` below) are not read by the library; transmit them with `setParameter`.

**Example:**

```text
www.website.com?wt_mc=Newsletter_2025_10&wt_cc1=link1
```

---

**Supported Methods**

| **Method** | **Description** | **Where to configure *** | **Where to analyze** |
|---|---|---|---|
| **constructor** | Initializes a campaign object. The campaign ID can be set via setId. | – | *Marketing > Campaigns > Advertising Media* |
| **setId** | Defines the campaign ID in the format mediaCodeName%3DmediaCodeValue (URL-encoded =), e.g. wt_mc%3Dnewsletter.2025.spring. | – | *Marketing > Campaigns > Advertising Media* |
| **setMediaCode** | Defines the names of the URL parameters (List&lt;String>) from which the campaign ID is read when no ID was set. Default: mc, wt_mc. | – | *Marketing > Campaigns > Advertising Media* |
| **setOncePerSession** | Transmits a campaign only once per session (cookie wtstp_tcc). | – | *Marketing > Campaigns > Advertising Media* |
| **setParameter** | Tracks additional campaign-specific information that always refers to an advertising medium. | CP > Own Configuration | – |
| **getId** / **getMediaCode** / **isOncePerSession** | Return the configured values. getId returns only the explicitly set ID, not the ID read from the URL. | – | – |

* **Configuration Path Abbreviations:**

- **CP** = *Mapp Q3 > Configuration > Custom Parameters > Campaign Parameters > Preconfigured > …*

**Generated request parameters**

| **Method** | **Request parameter** | **Constant in `MappIntelligenceParameter`** |
|---|---|---|
| **constructor** / **setId** / **setMediaCode** / **setOncePerSession** | `mc` (resolved campaign ID) | `CAMPAIGN_ID` |
| – | `mca` (always `c`) | `CAMPAIGN_ACTION` |
| **setParameter** | `cc<ID>` | `CUSTOM_CAMPAIGN_PARAMETER` |

The campaign ID is URL-encoded once more in the request, so `wt_mc%3Dfoo` is sent as `mc=wt_mc%253Dfoo`. `mca=c` is sent with every campaign object, even if no campaign ID was found.

> [!NOTE]
> Empty values and the values `"0"`, `"0.0"` and `"false"` are removed before the request is sent, so a campaign parameter with the value `"0"` or `"false"` is not transmitted. This is a known open issue of the current code.
>
> `null` counts as "not set": `setId(null)` behaves like no ID (the campaign is read from the media codes of the request URL), `setMediaCode(null)` means no media codes are read at all, and a `setParameter` value `null` is not transmitted. None of them throws an exception.
>
> The media code names are matched case-sensitively (`wt_mc` does not match `WT_MC`). The default list returned by `getMediaCode()` has a fixed size, so `getMediaCode().add(...)` throws an `UnsupportedOperationException`; pass a new list instead, e.g. `setMediaCode(new ArrayList<>(Arrays.asList("mc", "wt_mc", "utm_campaign")))`.

**Methods**

#### constructor

```java
MappIntelligenceCampaign mappCampaignData = new MappIntelligenceCampaign();
```

```java
/**
 * @param i A campaign ID consists of a media code name and its value, separated by "%3D"
 */
MappIntelligenceCampaign mappCampaignData = new MappIntelligenceCampaign(String i);
```

#### setId

```java
/**
 * @param i A campaign ID consists of a media code name and its value, separated by "%3D"
 *
 * @return MappIntelligenceCampaign
 */
mappCampaignData.setId(String i);
```

#### setMediaCode

```java
/**
 * @param mc If you use media codes as a data source for your campaign tracking, entering their name can
 *           raise the accuracy of the data collected (default: mc, wt_mc)
 *
 * @return MappIntelligenceCampaign
 */
mappCampaignData.setMediaCode(List<String> mc);
```

#### setOncePerSession

```java
/**
 * @param ops A campaign ID is tracked only once per session
 *
 * @return MappIntelligenceCampaign
 */
mappCampaignData.setOncePerSession(boolean ops);
```

#### setParameter

```java
/**
 * Campaign parameters always refer to an advertising medium.
 *
 * @param i    ID of the parameter
 * @param value Value of the parameter
 *
 * @return MappIntelligenceCampaign
 */
mappCampaignData.setParameter(int i, String value);
```

#### getId / getMediaCode / isOncePerSession

```java
String id = mappCampaignData.getId();
List<String> mediaCode = mappCampaignData.getMediaCode();
boolean oncePerSession = mappCampaignData.isOncePerSession();
```

**Example**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);
MappIntelligenceCampaign mappCampaignData = new MappIntelligenceCampaign();
mappCampaignData.setId("wt_mc%3Dfoo.bar.2")
    .setParameter(1, "parameter 1")
    .setParameter(2, "parameter 2");

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
    mit.track((new MappIntelligenceDataMap()).campaign(mappCampaignData));
}
```

**Example: campaign from the request URL, once per session**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);
// e.g. https://www.website.com/landing?wt_mc=newsletter.2025.spring
mic.setRequestURL(request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
if (request.getCookies() != null) {
	for (Cookie c : request.getCookies()) {
		mic.addCookie(c.getName(), c.getValue());
	}
}
MappIntelligenceCampaign mappCampaignData = new MappIntelligenceCampaign();
mappCampaignData.setMediaCode(Arrays.asList("mc", "wt_mc"))
    .setOncePerSession(true);

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
	mit.track((new MappIntelligenceDataMap()).campaign(mappCampaignData));

	MappIntelligenceCookie campaignCookie = mit.getTransmittedCampaignsCookie();
	if (campaignCookie != null) {
		Cookie responseCookie = new Cookie(campaignCookie.getName(), campaignCookie.getValue());
		responseCookie.setHttpOnly(campaignCookie.isHttpOnly());
		responseCookie.setSecure(campaignCookie.isSecure());
		responseCookie.setPath(campaignCookie.getPath());
		responseCookie.setMaxAge(campaignCookie.getMaxAge());
		response.addCookie(responseCookie);
	}
}
```
