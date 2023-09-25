package me.prisonranksxtests;



import me.prisonranksx.PrisonRanksX;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;



class PrisonRanksXTest {

    private PrisonRanksX plugin;
    @BeforeEach
    void setUp() {
        PrisonRanksX plugin = Mockito.mock(PrisonRanksX.class);


    }

    @Test
    void hello() {
        System.out.println("hi");

    }
    @AfterEach
    void tearDown() {
    }
}