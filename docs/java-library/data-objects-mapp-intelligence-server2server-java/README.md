# Data Objects

> Based on the public documentation <https://docs.mapp.com/docs/data-objects-mapp-intelligence-server2server-java> (retrieved 2026-09-29), updated to the current code of this repository.

This section describes all data objects that can be transmitted via the Mapp Intelligence Java library.

Each page explains one specific object type — such as Page, Event, Campaign, or Order — including supported parameters, configuration details, and code examples for implementation.

- [Page](page-data-object-java-library-s2s.md) — `MappIntelligencePage`
- [Event](event-data-object-java-library-s2s.md) — `MappIntelligenceAction`
- [Campaign](campaign-data-object-java-library-s2s.md) — `MappIntelligenceCampaign`
- [User](user-data-object-java-library-s2s.md) — `MappIntelligenceCustomer`
- [Product](product-data-object-java-library-s2s.md) — `MappIntelligenceProduct` / `MappIntelligenceProductCollection`
- [Order](order-data-object-java-library-s2s.md) — `MappIntelligenceOrder`
- [Session](session-data-object-java-library-s2s.md) — `MappIntelligenceSession`
- [Mapp Engage](mapp-engage-data-object-java-library-s2s.md) — `MappIntelligenceEngage`
- [Registration](registration-data-object-java-library-s2s.md) — `MappIntelligenceRegistration` (Mapp Engage user matching)
- [Request Parameters](request-parameters-data-object-java-library-s2s.md) — `MappIntelligenceParameter` / `MappIntelligenceParameterMap`

All data object classes are in the package `com.mapp.intelligence.tracking.data`; the constant classes `MappIntelligenceParameter` and `MappIntelligenceCustomParameter` are in the package `com.mapp.intelligence.tracking`. They are combined in a `MappIntelligenceDataMap` (methods `page`, `action`, `campaign`, `customer`, `product`, `order`, `session`, `registration`, `engage`) and passed to `MappIntelligenceTracking.track(MappIntelligenceDataMap)`. Each method of the data map holds one object per type; calling it again replaces the previous object.

> [!NOTE]
> Before a request is built, every data object except `MappIntelligenceProduct` removes all values that are exactly `""`, `"0"`, `"0.0"`, `"false"` or `"{}"`. This also applies to custom parameters, categories and goals. As a result, a `false` boolean, a numeric `0` and a custom value `"0"` are never transmitted by these objects. This is a known open issue of the current code. Parameters passed with `track(MappIntelligenceParameterMap)` are not filtered (see [Request Parameters](request-parameters-data-object-java-library-s2s.md)).

> [!NOTE]
> `null` counts as "not set" in all data objects: a setter, a `setParameter`/`setCategory`/`setGoal` value or a `MappIntelligenceParameterMap.add()` key or value that is `null` is not transmitted, and `MappIntelligenceProductCollection.add(null)` is ignored. No `NullPointerException` is thrown.
