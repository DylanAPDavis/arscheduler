#!/bin/sh
ovs-vsctl --no-wait init
ovs-vswitchd --pidfile --detach