# Page

> Based on the public documentation <https://docs.mapp.com/docs/page-data-object-java-library-s2s> (retrieved 2026-09-29), updated to the current code of this repository.

Page requests send page-related data such as the page name, parameters, or categories to Mapp Intelligence. These elements help structure your website tracking and enable deeper analysis of user behavior.

This configuration applies to Server-to-Server integrations.

---

## Page Naming

By default, the library derives the page name from the URL, excluding any parameters.

For example, a URL like www.website.com/product_abc.htm?sid=7af49 is tracked as www.website.com/product_abc.htm.

If you want to use a custom name, define it with setName. When left blank, the library automatically generates the page name.

Details of the default page name:

- It is derived from the request URL configured with `setRequestURL` (see [Integration and Configuration](../integration-and-configuration.md)), without the protocol and converted to lower case.
- URL parameters listed with `setUseParamsForDefaultPageName` / `addUseParamsForDefaultPageName` are appended (e.g. `www.website.com/product_abc.htm?sid=7af49`) if they have a value in the request URL.
- If no request URL is configured, the page name is `0`.

The page name is not sent as a separate parameter but as part of the mandatory `p` parameter of the request (`p=600,<page name>,...`).

---

## Categories (Content Groups)

Categories (also known as *content groups*) classify pages into structured groups.

These groups must be configured in Mapp Q3 before they can be tracked via the library. Find more information [here](https://docs.mapp.com/docs/how-to-set-up-categories).

When implementing, include the **ID number** and the respective **value** as a string. The value should not exceed 255 characters; additional characters are truncated by Mapp Intelligence (the library itself does not shorten the value).

> [!NOTE]
> Content groups are assigned once to a page. If a page is tracked with a content group, all subsequent page calls will also be assigned to this content group. Only the first tracked value will be shown. Mapp therefore recommends implementing content IDs and content groups while embedding the library. See also [How can I replace missing values ("-") in categories?](https://docs.mapp.com/docs/how-can-i-replace-missing-values-in-categories)

Mapp recommends implementing content IDs and content groups during the initial library setup.

---

## Page Parameters

Page parameters refer to individual pages and can pass different values with each request. Unlike content groups, parameters can vary between page calls, making them useful for dynamic content or detailed tracking.

They must also be configured in Mapp Q3 before tracking. Find more information [here](https://docs.mapp.com/docs/how-to-set-up-custom-parameters).

When implementing, include the **ID number** and the **value** as a string.

---

## Goals

Website goals measure the success of specific actions on your website. They allow you to analyze user behavior and attribute conversions to campaigns.

Goals must be configured before they can be tracked via the library. Find more information [here](https://docs.mapp.com/docs/how-to-define-website-goals).

When configuring tracking, include the **goal ID** and the **value** as a string.

Each goal is considered achieved as soon as a value is tracked—regardless of the actual content of that value.

> [!NOTE]
> The predefined goal Order cannot be tracked within the page object.
>
> Avoid submitting values like "no" for goals such as Newsletter Registration, as this would still mark the goal as achieved.
>
> Goals are transmitted as E-Commerce Parameters (`cb<ID>`). Page goals, event goals and product or order E-Commerce Parameters share this namespace: if several objects of the same request use the same ID, only one of the values is sent.

---

**Supported Methods**

| **Method** | **Description** | **Where to configure *** | **Where to analyze** |
|---|---|---|---|
| **constructor** | Defines the initial page request and can overwrite the default page naming. | – | *Navigation > Pages > Pages* |
| **setName** | Overwrites the default page naming. | – | *Navigation > Pages > Pages* |
| **setSearch** | Tracks search terms entered in the internal website search. | – | *Marketing > Search Phrases > Internal Search Phrases* |
| **setNumberSearchResults** | Tracks the number of results returned for an internal search query. | PP > Number of search results | Metric: [Name of Parameter] |
| **setErrorMessages** | Tracks system or content-related error messages. | PP > Error messages | *Navigation > Page Parameters > [Name]* |
| **setPaywall** | Tracks whether an article is behind a paywall. | PP > Paywall calls | *Navigation > Page Parameters > [Name]* |
| **setArticleTitle** | Captures the article headline. | PP > Article heading | *Navigation > Page Parameters > [Name]* |
| **setContentTags** | Tracks article tags or keywords. | PP > Content tags | *Navigation > Page Parameters > [Name]* |
| **setTitle** | Captures the page title for reporting and analysis. | PP > Page title | *Navigation > Page Parameters > [Name]* |
| **setType** | Defines the page type (e.g. “overview”). | PP > Page type | *Navigation > Page Parameters > [Name]* |
| **setLength** | Classifies the page length (e.g. “large”). | PP > Page length | *Navigation > Page Parameters > [Name]* |
| **setDaysSincePublication** | Tracks how many days have passed since publication. | PP > Days since publication | *Navigation > Page Parameters > [Name]* |
| **setTestVariant** | Stores the name of a test variant (A/B testing). | PP > Variant | *Navigation > Page Parameters > [Name]* |
| **setTestExperiment** | Stores the name of an experiment (A/B testing). | PP > Experiment | *Navigation > Page Parameters > [Name]* |
| **setParameter** | Tracks custom page-specific information or metrics defined by the website. | PP > Own Configuration | Text: *Navigation > Page Parameters > [Name]* / Number: Metric |
| **setCategory** | Groups pages into structured website sections (*Content Groups*). | CG > New Category | Text: *Navigation > Content Groups > [Name]* / Number: Metric |
| **setGoal** | Tracks website goals such as registrations or purchases. | EC > Own Configuration | Text: *E-Commerce > E-Commerce Parameters > [Name]* / Number: Metric |

* **Configuration Path Abbreviations:**

- **PP** = *Mapp Q3 > Configuration > Custom Parameters > Page Parameters > Preconfigured > …*
- **CG** = *Mapp Q3 > Configuration > Categories > Content Groups > …*
- **EC** = *Mapp Q3 > Configuration > Custom Parameters > E-Commerce Parameters > Preconfigured > …*

**Generated request parameters**

| **Method** | **Request parameter** | **Constant in `MappIntelligenceParameter`** |
|---|---|---|
| **constructor** / **setName** | part of `p` (`p=600,<name>,...`) | `PAGE_NAME` (`pn`, used internally only) |
| **setSearch** | `is` | `SEARCH` |
| **setNumberSearchResults** | `cp771` | `NUMBER_SEARCH_RESULTS` |
| **setErrorMessages** | `cp772` | `ERROR_MESSAGES` |
| **setPaywall** | `cp773` (`true`) | `PAYWALL` |
| **setArticleTitle** | `cp774` | `ARTICLE_TITLE` |
| **setContentTags** | `cp775` | `CONTENT_TAGS` |
| **setTitle** | `cp776` | `PAGE_TITLE` |
| **setType** | `cp777` | `PAGE_TYPE` |
| **setLength** | `cp778` | `PAGE_LENGTH` |
| **setDaysSincePublication** | `cp779` | `DAYS_SINCE_PUBLICATION` |
| **setTestVariant** | `cp781` | `TEST_VARIANT` |
| **setTestExperiment** | `cp782` | `TEST_EXPERIMENT` |
| **setParameter** | `cp<ID>` | `CUSTOM_PAGE_PARAMETER` |
| **setCategory** | `cg<ID>` | `CUSTOM_PAGE_CATEGORY` |
| **setGoal** | `cb<ID>` | `CUSTOM_PRODUCT_PARAMETER` |

> [!NOTE]
> Empty values and the values `"0"`, `"0.0"` and `"false"` are removed before the request is sent. `setNumberSearchResults(0)`, `setDaysSincePublication(0)` and `setPaywall(false)` are therefore not transmitted, and neither is a parameter, category or goal with the value `"0"` or `"false"`. This is a known open issue of the current code.
>
> `null` counts as "not set": a string setter or a `setParameter`/`setCategory`/`setGoal` value called with `null` is not transmitted (no exception).
>
> The predefined page parameters use the IDs 771–782. Do not use these IDs with `setParameter`, otherwise the values overwrite each other.

**Methods**

#### constructor

```java
MappIntelligencePage mappPageData = new MappIntelligencePage();
```

```java
/**
 * MappIntelligencePage constructor.
 *
 * @param n Allows to overwrite the default page naming
 */
MappIntelligencePage mappPageData = new MappIntelligencePage(String n);
```

#### setName

```java
/**
 * @param n Allows to overwrite the default page naming
 *
 * @return MappIntelligencePage
 */
mappPageData.setName(String n);
```

#### setSearch

```java
/**
 * @param s Search terms used in internal search
 *
 * @return MappIntelligencePage
 */
mappPageData.setSearch(String s);
```

#### setNumberSearchResults

```java
/**
 * @param nSearchResults Number of internal search results
 *
 * @return MappIntelligencePage
 */
mappPageData.setNumberSearchResults(int nSearchResults);
```

#### setErrorMessages

```java
/**
 * @param eMessages Allows to track error messages
 *
 * @return MappIntelligencePage
 */
mappPageData.setErrorMessages(String eMessages);
```

#### setPaywall

```java
/**
 * @param p Allows to mark an article, if it is behind the Paywall
 *
 * @return MappIntelligencePage
 */
mappPageData.setPaywall(boolean p);
```

#### setArticleTitle

```java
/**
 * @param aTitle Article heading
 *
 * @return MappIntelligencePage
 */
mappPageData.setArticleTitle(String aTitle);
```

#### setContentTags

```java
/**
 * @param cTags Tags of an article
 *
 * @return MappIntelligencePage
 */
mappPageData.setContentTags(String cTags);
```

#### setTitle

```java
/**
 * @param t Title of the page
 *
 * @return MappIntelligencePage
 */
mappPageData.setTitle(String t);
```

#### setType

```java
/**
 * @param t Type of the page (e.g. "overview")
 *
 * @return MappIntelligencePage
 */
mappPageData.setType(String t);
```

#### setLength

```java
/**
 * @param l Length of the page (e.g. "large")
 *
 * @return MappIntelligencePage
 */
mappPageData.setLength(String l);
```

#### setDaysSincePublication

```java
/**
 * @param dSincePublication Days since publication
 *
 * @return MappIntelligencePage
 */
mappPageData.setDaysSincePublication(int dSincePublication);
```

#### setTestExperiment

```java
/**
 * @param tExperiment Name of the test
 *
 * @return MappIntelligencePage
 */
mappPageData.setTestExperiment(String tExperiment);
```

#### setTestVariant

```java
/**
 * @param tVariant Name of the test variant
 *
 * @return MappIntelligencePage
 */
mappPageData.setTestVariant(String tVariant);
```

#### setParameter

```java
/**
 * You can use parameters to enrich Mapp Intelligence data with your own website-specific information and/or metrics.
 *
 * @param id    ID of the parameter
 * @param value Value of the parameter
 * @return MappIntelligencePage
 */
mappPageData.setParameter(int id, String value);
```

#### setCategory

```java
/**
 * Page categories (called "Content Groups" in Mapp) are used to group pages to create website areas.
 *
 * @param id    ID of the parameter
 * @param value Value of the parameter
 * @return MappIntelligencePage
 */
mappPageData.setCategory(int id, String value);
```

#### setGoal

```java
/**
 * When using website goals, all central goals are quickly available for analyzing and filtering.
 *
 * @param id    ID of the parameter
 * @param value Value of the parameter
 * @return MappIntelligencePage
 */
mappPageData.setGoal(int id, String value);
```

**Example**

```java
MappIntelligenceConfig mic = new MappIntelligenceConfig(
	"111111111111111",
	"analytics01.wt-eu02.net"
);
MappIntelligencePage mappPageData = new MappIntelligencePage("name");
mappPageData.setName("name of a page")
    .setSearch("search term")
    .setNumberSearchResults(15)
    .setErrorMessages("error message")
    .setPaywall(true)
    .setArticleTitle("name of an article")
    .setContentTags("name of a content tag")
    .setTitle("title of a page")
    .setType("type of a page")
    .setLength("large")
    .setDaysSincePublication(8)
    .setTestVariant("name of a variant")
    .setTestExperiment("name of an experiment")
    .setParameter(1, "parameter 1")
    .setParameter(2, "parameter 2")
    .setCategory(1, "category 1")
    .setCategory(2, "category 2")
    .setGoal(1, "goal 1")
    .setGoal(2, "goal 2");

try (MappIntelligenceTracking mit = new MappIntelligenceTracking(mic)) {
    mit.track((new MappIntelligenceDataMap()).page(mappPageData));
}
```
