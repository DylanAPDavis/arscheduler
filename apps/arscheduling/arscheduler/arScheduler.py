__author__ = 'dylan'
# Take in input for submitting Flow Scheduling requests, verify that input is in correct format
# Submit to REST endpoint

import os
import argparse
import re
import json

controller_ip = '127.0.0.1'
controller_OF_port = 6653
controller_rest_port = 8080
rest_end_point = "http://{0}:{1}/wm/arscheduler/schedule/json".format(controller_ip, controller_rest_port)


def verify_input(input_args):
    response = ""
    if check_ip(input_args.srcIP) is None:
        response += "Error: {0} does not match format {1}\n".format("Source IP", "e.g. 10.0.0.1")
    if check_mac(input_args.srcMAC) is None:
        response += "Error: {0} does not match format {1}\n".format("Source MAC", "e.g. 00:00:00:00:00:01")
    if check_ip(input_args.dstIP) is None:
        response += "Error: {0} does not match format {1}\n".format("Destination IP", "e.g. 10.0.0.2")
    if check_mac(input_args.dstMAC) is None:
        response += "Error: {0} does not match format {1}\n".format("Destination MAC", "e.g. 00:00:00:00:00:02")
    if check_bandwidth(input_args.bandwidth) is None:
        response += "Error: {0} does not match format {1}\n".format("Bandwidth", "e.g. 2")
    if check_time(input_args.startTime) is None:
        response += "Error: {0} does not match format {1}\n".format("Start Time", "e.g. 11:20")
    if check_time(input_args.endTime) is None:
        response += "Error: {0} does not match format {1}\n".format("End Time", "e.g. 13:27")

    return response


def check_ip(ip_string):
    pattern = re.compile("^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$")
    return pattern.match(ip_string)


def check_mac(mac_string):
    pattern = re.compile("^\d{2}:\d{2}:\d{2}:\d{2}:\d{2}:\d{2}$")
    return pattern.match(mac_string)


def check_time(time_string):
    pattern = re.compile("^\d{2}:\d{2}$")
    return pattern.match(time_string)


def check_bandwidth(bw_string):
    pattern = re.compile("^\d{1,10}$")
    return pattern.match(bw_string)


def build_parser():
    arg_parser = argparse.ArgumentParser(description="Submit a Flow Scheduling Request to the Floodlight Controller")
    arg_parser.add_argument("srcIP", help="IP address of source host (e.g. 10.0.0.1)")
    arg_parser.add_argument("srcMAC", help="MAC address of source host (e.g. 00:00:00:00:00:01)")
    arg_parser.add_argument("dstIP", help="IP address of destination host (e.g. 10.0.0.2)")
    arg_parser.add_argument("dstMAC", help="MAC address of destination host (e.g. 00:00:00:00:00:02)")
    arg_parser.add_argument("bandwidth", help="Requested bandwidth in Gbps (e.g. 2 Gbps)")
    arg_parser.add_argument("startTime", help="Start time in HH:mm format (e.g. 11:20 for 11:20 AM)")
    arg_parser.add_argument("endTime", help="Ending time in HH:mm format (e.g. 13:27 for 1:27 PM)")
    return arg_parser


def submit_request(input_args):
    src_ip = input_args.srcIP
    src_mac = input_args.srcMAC
    dst_ip = input_args.dstIP
    dst_mac = input_args.dstMAC
    bandwidth = input_args.bandwidth
    start_time = input_args.startTime
    end_time = input_args.endTime
    schedule_command = "curl -X POST -d '{{ \"{0}\": \"{1}\", \"{2}\": \"{3}\", \"{4}\": \"{5}\", \"{6}\": \"{7}\", " \
                       "\"{8}\": \"{9}\", \"{10}\": \"{11}\", \"{12}\": \"{13}\" }}' ".format("srcIP", src_ip, "srcMac",
                                                                                              src_mac, "dstIP", dst_ip,
                                                                                              "dstMac", dst_mac,
                                                                                              "bandwidth", bandwidth,
                                                                                              "startTime", start_time,
                                                                                              "endTime", end_time)
    schedule_command += rest_end_point
    try:
        result = json.loads(os.popen(schedule_command).read())
        print(result)
    except ValueError:
        print("Error: controller unreachable")

if __name__ == "__main__":

    parser = build_parser()
    args = parser.parse_args()

    # Check that arguments are valid
    verification_response = verify_input(args)
    if verification_response != "":
        print(verification_response)
        exit()

    submit_request(args)

    exit()
