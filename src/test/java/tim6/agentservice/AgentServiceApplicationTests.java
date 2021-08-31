package tim6.agentservice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import tim6.agentservice.ads.integration.CreateAdTest;
import tim6.agentservice.ads.integration.GetAdsTest;

@RunWith(Suite.class)
@SuiteClasses({
        CreateAdTest.class,
        GetAdsTest.class
})
class AgentServiceApplicationTests {


}
