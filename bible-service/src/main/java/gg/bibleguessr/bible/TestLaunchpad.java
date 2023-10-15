package gg.bibleguessr.bible;

import gg.bibleguessr.bible.objs.Version;
import gg.bibleguessr.service_wrapper.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLaunchpad {

   public static void main(String[] args) {

      Logger logger = LoggerFactory.getLogger(TestLaunchpad.class.getSimpleName());
      BibleService service = new BibleService();

      for (Version version : service.getVersions()) {
         System.out.println("Version: " + version.getName());
      }

   }

}
