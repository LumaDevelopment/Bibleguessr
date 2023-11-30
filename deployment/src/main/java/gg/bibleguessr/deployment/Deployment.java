package gg.bibleguessr.deployment;

import gg.bibleguessr.bible.BibleService;
import gg.bibleguessr.guess_counter.GuessCounterService;
import gg.bibleguessr.service_wrapper.ServiceWrapper;

public class Deployment {
    public static void main(String[] args) {
        ServiceWrapper wrapper = new ServiceWrapper();
        wrapper.run(new BibleService());
        wrapper.run(new GuessCounterService());
    }
}