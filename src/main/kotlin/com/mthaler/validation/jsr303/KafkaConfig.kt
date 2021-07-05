package com.mthaler.validation.jsr303

import com.google.common.net.HostAndPort

class KafkaConfig(var bootstrapServers: List<HostAndPort>, var applicationId: String)