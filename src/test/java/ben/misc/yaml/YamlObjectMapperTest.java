package ben.misc.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class YamlObjectMapperTest {

    final static YAMLMapper mapper = new YAMLMapper();

    static String getYamlString(String resource) throws IOException {
        return Resources.toString(Resources.getResource(resource), Charsets.UTF_8);
    }

    @Test
    public void canDeserializeUser() throws Exception {
        User user = mapper.readValue(getYamlString("yaml/user.yml"), User.class);
        assertTrue(user.age.equals(29));
        assertEquals(user.hobbies.get(2), "Java");
    }

    @Test
    public void cannotSerializeMultilineString() throws Exception {
        Map<String, Object> content = new HashMap<String, Object>();
        content.put("key", "first\nsecond\nthird");
        String yaml = mapper.writeValueAsString(content).trim();

        /**
         * In the 2.7.0 release of jackson-dataformat-yaml,
         * this assertion should fail due to added support for multiline string
         */
        assertNotEquals("---\n" + "key: |-\n  first\n  second\n  third", yaml);
    }


    static class User {
        String name;
        Integer age;
        List<String> hobbies;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public List<String> getHobbies() {
            return hobbies;
        }

        public void setHobbies(List<String> hobbies) {
            this.hobbies = hobbies;
        }
    }

}
