__author__ = 'dylan'

from mininet.topo import Topo
# Collection of Topologies that can be created using Mininet


# Linear with three switches, two hosts on S1 and S2, one host on S3
class TestTopology1(Topo):
    num_hosts = 0
    switch_link_dictionary = {
        "s1": ["s2"],
        "s2": ["s3"],
        "s3": []
    }

    def build(self):
        switches = []
        for s in range(1, 4):
            # 'switch' is the name of the new switch
            switch = self.addSwitch(name='s{0}'.format(s))
            if s == 1 or s == 2:
                self.add_hosts(switch, 2)
            else:
                self.add_hosts(switch, 1)
            switches.append(switch)
        self.add_switch_links(switches)

    def add_hosts(self, switch, num_hosts_needed):
        for h in range(num_hosts_needed):
            self.num_hosts += 1
            host = self.addHost("h{0}".format(self.num_hosts))
            self.addLink(host, switch)

    def add_switch_links(self, switches):
        for switch_name in switches:
            connected_switch_names = self.switch_link_dictionary[switch_name]

            for connected_switch_name in connected_switch_names:
                self.addLink(switch_name, connected_switch_name)


# "Diamond" topology, with S1 connected to S2 and S3, and both S2/S3 connected to S4
class TestTopology2(TestTopology1):
    switch_link_dictionary = {
        "s1": ["s2", "s3"],
        "s2": ["s4"],
        "s3": ["s4"],
        "s4": []
    }

    def build(self):
        switches = {}
        for s in range(1, 5):
            switch = self.addSwitch('s{0}'.format(s))
            self.add_hosts(switch, 1)
            switches["s{0}".format(s)] = switch
        self.add_switch_links(switches)


# Linear four switch topology, one host per switch
class TestTopology3(TestTopology2):
    switch_link_dictionary = {
        "s1": ["s2"],
        "s2": ["s3"],
        "s3": ["s4"],
        "s4": []
    }


# Single switch topology with two hosts
class TestTopology4(TestTopology2):
    switch_link_dictionary = {
        "s1": []
    }

    def build(self):
        switches = {}
        switch = self.addSwitch('s{0}'.format(1))
        self.add_hosts(switch, 2)
        switches["s{0}".format(1)] = switch