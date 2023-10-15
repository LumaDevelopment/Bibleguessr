package gg.bibleguessr.bible;

import gg.bibleguessr.bible.objs.Version;
import gg.bibleguessr.service_wrapper.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLaunchpad {

   public static void main(String[] args) {

      Logger logger = LoggerFactory.getLogger(TestLaunchpad.class.getSimpleName());
      BibleService service = new BibleService();

      int startUniversalIndex = Bible.getInstance().getVerseByReference(0, 1, 1).universalIndex();
      int endUniversalIndex = Bible.getInstance().getVerseByReference(0, 1, 3).universalIndex();

      for (Version version : service.getVersions()) {
         logger.info(
               "Genesis 1:1-3 in {}: {}",
               version.getName(),
               service.getBibleReadingMgr().getPassageText(version, startUniversalIndex, endUniversalIndex)
         );
      }

   }

}
