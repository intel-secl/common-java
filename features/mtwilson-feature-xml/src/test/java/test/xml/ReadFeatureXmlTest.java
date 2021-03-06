/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package test.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import com.intel.mtwilson.feature.xml.*;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;


/**
 * 
 *
 * @author jbuhacoff
 */
public class ReadFeatureXmlTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReadFeatureXmlTest.class);

    // probably should be moved to a new utility module cpg-xml or cpg-jaxb 
    
    // this pair is used when root element has @XmlRootEelment annotation when building
    private <T> T fromRootXML(InputStream in, Class<T> valueType) throws IOException, JAXBException {
        String document = IOUtils.toString(in, "UTF-8");
        return fromRootXML(document, valueType);
    }
    private <T> T fromRootXML(String document, Class<T> valueType) throws IOException, JAXBException {
        JAXBContext jc = JAXBContext.newInstance( valueType );
        Unmarshaller u = jc.createUnmarshaller();
        Object o = u.unmarshal( new StreamSource( new StringReader( document ) ) ); // commented otu due to "Expected elements are (none)" error since xjc does not annotate root element <selections> with a tag and maybe timestamp.
        return (T)o;
    }
    
    // probably should be moved to a new utility module cpg-xml or cpg-jaxb 
    
    // this pair is used when root element does NOT have @XmlRootEelment annotation when building
    private <T> T fromXML(InputStream in, Class<T> valueType) throws IOException, JAXBException {
        String document = IOUtils.toString(in, "UTF-8");
        return fromXML(document, valueType);
    }
    private <T> T fromXML(String document, Class<T> valueType) throws IOException, JAXBException {
        JAXBContext jc = JAXBContext.newInstance( valueType );
        Unmarshaller u = jc.createUnmarshaller();
        JAXBElement<T> e = u.unmarshal( new StreamSource( new StringReader( document ) ), valueType);
        return e.getValue();
    }
    
    /**
     * Sample output for feature 1:
     * 
{"id":"feature1","version":"0.1","name":"Feature #1","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An example feature","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.","url":"file:///LICENSE.TXT"},"includes":[],"requires":[],"conflicts":[],"links":[],"settings":[],"extends":[]}
     * 
     * Sample output for feature 2:
     * 
{"id":"feature2","version":"0.1","name":"Feature #2","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"A second example feature which depends on the first feature","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.","url":"file:///LICENSE.TXT"},"includes":null,"requires":{"feature_ref":{"id":"feature1","version":null}},"conflicts":null,"links":null}
     * 
     * Sample output for feature 3:
     * 
{"id":"feature3","version":"0.1","name":"Feature #3","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"A third example feature which depends on the first feature and conflicts with the second feature","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.","url":"file:///LICENSE.TXT"},"includes":null,"requires":{"feature_ref":{"id":"feature1","version":null}},"conflicts":{"feature_ref":{"id":"feature2","version":null}},"links":null}
     * 
     * Sample output for feature 4:
     * 
{"id":"feature4","version":"0.1","name":"Feature #4","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An example feature","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.","url":"file:///LICENSE.TXT"},"includes":{"components":{"component":{"id":"componentA","version":"0.76","name":"Component A","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An important sub-component","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.","url":"file:///LICENSE.TXT"},"includes":null,"requires":null,"conflicts":null,"links":null}}},"requires":null,"conflicts":null,"links":{"link":{"value":"mailto:feature4-users@provider.com","rel":"mailing list","type":null}}}
{"id":"feature4","version":"0.1","name":"Feature #4","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An example feature","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.","url":"file:///LICENSE.TXT"},"includes":{"components":{"component":{"id":"componentA","version":"0.76","name":"Component A","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An important sub-component","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.","url":"file:///LICENSE.TXT"},"includes":null,"requires":null,"conflicts":null,"links":null}}},"requires":null,"conflicts":null,"links":{"link":{"href":"mailto:feature4-users@provider.com","rel":"mailing list","type":null}}}
     * 
     * Sample output for feature 5:
     * 
 {"id":"feature4","version":"0.1","name":"Feature #4","provider":{"name":"Intel","url":"http://www.intel.com"},
 * "description":"An example feature",
 * "license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.",
 * "url":"file:///LICENSE.TXT"},
 * "includes":{"components":{"component":{"id":"componentA","version":"0.76","name":"Component A","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An important sub-component","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.","url":"file:///LICENSE.TXT"},"includes":null,"requires":null,"conflicts":null,"links":null,"settings":null,"extends":null}}},
 * "requires":{"feature_ref":{"id":"feature2","version":null}},
 * "conflicts":{"feature_ref":{"id":"feature4","version":null}},"links":{"link":{"href":"mailto:feature4-users@provider.com","rel":"mailing list","type":null}},
 * "settings":{"setting":{"name":"com.example.setting3","required":null,"type":"integer"}},"extends":null}     * 
     * 
     * Sample output for feature 6 without jaxb-xew-plugin:
     * 
{"id":"feature4","version":"0.1","name":"Feature #4","provider":{"name":"Intel","url":"http://www.intel.com"}
* ,"description":"An example feature","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.",
* "url":"file:///LICENSE.TXT"},"includes":null,"requires":{"feature_ref":[{"id":"feature1","version":null},
* {"id":"feature2","version":null}]},"conflicts":{"feature_ref":[{"id":"feature3","version":null},
* {"id":"feature4","version":null}]},"links":{"link":[{"href":"http://www.provider.com/support","rel":"issues","type":null},
* {"href":"mailto:feature4-users@provider.com","rel":"mailing list","type":null}]},
* "settings":{"setting":[{"name":"com.example.setting1","required":null,"type":null},
* {"name":"com.example.setting2","required":true,"type":null},
* {"name":"com.example.setting3","required":null,"type":"integer"}]},"extends":null}     * 
     * 
     * Sample output for feature 6 WITH jaxb-xew-plugin:
     * 
{"id":"feature4","version":"0.1","name":"Feature #4","provider":{"name":"Intel","url":"http://www.intel.com"},
* "description":"An example feature","license":{"copyright":"2019 Intel Corporation. SPDX-License-Identifier: BSD-3-Clause.",
* "url":"file:///LICENSE.TXT"},"includes":[],
* "requires":[{"id":"feature1","version":null},
* {"id":"feature2","version":null}],
* "conflicts":[{"id":"feature3","version":null},{"id":"feature4","version":null}],
* "links":[{"href":"http://www.provider.com/support","rel":"issues","type":null},
* {"href":"mailto:feature4-users@provider.com","rel":"mailing list","type":null}],
* "settings":[{"name":"com.example.setting1","required":null,"type":null},
* {"name":"com.example.setting2","required":true,"type":null},
* {"name":"com.example.setting3","required":null,"type":"integer"}],"extends":[]}
* 
     * 
     * 
     * @throws Exception
     */    
    @Test
    public void testReadFeatureType() throws Exception {
        InputStream in = getClass().getResourceAsStream("/feature-xml-examples/feature1.xml");
        Feature feature = fromXML(in, Feature.class);
        in.close();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        log.debug("feature: {}", mapper.writeValueAsString(feature));
    }
}
