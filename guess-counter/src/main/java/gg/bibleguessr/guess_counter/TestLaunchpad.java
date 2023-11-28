package gg.bibleguessr.guess_counter;

import gg.bibleguessr.service_wrapper.ServiceWrapper;

public class TestLaunchpad {

    public static void main(String[] args) {
        new ServiceWrapper().run(new GuessCounterService());
    }

}
