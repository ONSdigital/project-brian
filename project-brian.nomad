job "project-brian" {
  datacenters = ["eu-west-1"]
  region      = "eu"
  type        = "service"

  update {
    stagger      = "90s"
    max_parallel = 1
  }

  group "publishing" {
    count = 1

    constraint {
      attribute = "${node.class}"
      value     = "publishing"
    }

    task "project-brian" {
      driver = "docker"

      artifact {
        source = "s3::https://s3-eu-west-1.amazonaws.com/{{DEPLOYMENT_BUCKET}}/project-brian/{{REVISION}}.tar.gz"
      }

      config {
        command = "${NOMAD_TASK_DIR}/start-task"

        args = [
          "java",
          "-Xmx4094m",
          "-Drestolino.packageprefix=com.github.onsdigital.brian.api",
          "-jar target/*-jar-with-dependencies.jar",
        ]

        image = "{{ECR_URL}}:concourse-{{REVISION}}"

        port_map {
          http = 8080
        }
      }

      service {
        name = "project-brian"
        port = "http"
        tags = ["publishing"]
      }

      resources {
        cpu    = 1500
        memory = 4096

        network {
          port "http" {}
        }
      }

      template {
        source      = "${NOMAD_TASK_DIR}/vars-template"
        destination = "${NOMAD_TASK_DIR}/vars"
      }

      vault {
        policies = ["project-brian"]
      }
    }
  }
}