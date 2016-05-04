__author__ = 'dylan'
# Sets of Flow Scheduling requests used to test ARScheduling module

test_dictionary = {
    1: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "1", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "2", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "3", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "2", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "1", "00:00", "00:01")
        ],
    2: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "1", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "2", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "3", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "4", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "1", "00:00", "00:01")
        ],
    3: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.3", "00:00:00:00:00:03", "1", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "2", "00:00", "00:01"),
        ("10.0.0.2", "00:00:00:00:00:02", "10.0.0.3", "00:00:00:00:00:03", "3", "00:00", "00:01"),
        ("10.0.0.2", "00:00:00:00:00:02", "10.0.0.4", "00:00:00:00:00:04", "2", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.3", "00:00:00:00:00:03", "1", "00:00", "00:01")
        ],
    4: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.3", "00:00:00:00:00:03", "4", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "4", "00:00", "00:01"),
        ("10.0.0.2", "00:00:00:00:00:02", "10.0.0.3", "00:00:00:00:00:03", "2", "00:00", "00:01"),
        ("10.0.0.2", "00:00:00:00:00:02", "10.0.0.4", "00:00:00:00:00:04", "1", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.3", "00:00:00:00:00:03", "1", "00:00", "00:01")
        ],
    5: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.5", "00:00:00:00:00:05", "1", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.5", "00:00:00:00:00:05", "2", "00:00", "00:01"),
        ("10.0.0.3", "00:00:00:00:00:03", "10.0.0.5", "00:00:00:00:00:05", "3", "00:00", "00:01"),
        ("10.0.0.3", "00:00:00:00:00:03", "10.0.0.5", "00:00:00:00:00:05", "2", "00:00", "00:01"),
        ("10.0.0.4", "00:00:00:00:00:04", "10.0.0.5", "00:00:00:00:00:05", "1", "00:00", "00:01")
        ],
    6: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.5", "00:00:00:00:00:05", "7", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.5", "00:00:00:00:00:05", "4", "00:00", "00:01"),
        ("10.0.0.3", "00:00:00:00:00:03", "10.0.0.5", "00:00:00:00:00:05", "3", "00:00", "00:01"),
        ("10.0.0.3", "00:00:00:00:00:03", "10.0.0.5", "00:00:00:00:00:05", "4", "00:00", "00:01"),
        ("10.0.0.4", "00:00:00:00:00:04", "10.0.0.5", "00:00:00:00:00:05", "1", "00:00", "00:01")
        ],
    7: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "8", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "2", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "7", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "2", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "1", "00:00", "00:01")
        ],
    8: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "8", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "4", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "7", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "3", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "1", "00:00", "00:01")
        ],
    9: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "1", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.3", "00:00:00:00:00:03", "2", "00:00", "00:01"),
        ("10.0.0.2", "00:00:00:00:00:02", "10.0.0.3", "00:00:00:00:00:03", "3", "00:00", "00:01"),
        ("10.0.0.4", "00:00:00:00:00:04", "10.0.0.1", "00:00:00:00:00:01", "2", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "1", "00:00", "00:01")
        ],
    10: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.2", "00:00:00:00:00:02", "6", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.3", "00:00:00:00:00:03", "6", "00:00", "00:01"),
        ("10.0.0.2", "00:00:00:00:00:02", "10.0.0.3", "00:00:00:00:00:03", "6", "00:00", "00:01"),
        ("10.0.0.4", "00:00:00:00:00:04", "10.0.0.1", "00:00:00:00:00:01", "6", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "6", "00:00", "00:01")
        ],
    11: [("10.0.0.1", "00:00:00:00:00:01", "10.0.0.4", "00:00:00:00:00:04", "5", "00:00", "00:01"),
        ("10.0.0.2", "00:00:00:00:00:02", "10.0.0.3", "00:00:00:00:00:03", "2", "00:00", "00:01"),
        ("10.0.0.1", "00:00:00:00:00:01", "10.0.0.3", "00:00:00:00:00:03", "1", "00:00", "00:01")
    ]
}