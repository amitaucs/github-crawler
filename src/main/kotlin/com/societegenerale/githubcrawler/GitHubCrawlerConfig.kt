package com.societegenerale.githubcrawler


import com.societegenerale.githubcrawler.output.GitHubCrawlerOutput
import com.societegenerale.githubcrawler.ownership.NoOpOwnershipParser
import com.societegenerale.githubcrawler.ownership.OwnershipParser
import com.societegenerale.githubcrawler.remote.RemoteGitHub
import com.societegenerale.githubcrawler.remote.RemoteGitHubImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.ConversionService
import org.springframework.core.env.Environment

@Configuration
@EnableConfigurationProperties(GitHubCrawlerProperties::class)
@ComponentScan("com.societegenerale.githubcrawler.parsers")
open class GitHubCrawlerConfig {

    val log = LoggerFactory.getLogger(this.javaClass)

    @Bean
    open fun conversionService(): ConversionService {
        //TODO custom converter should be an annotation if possible - right now, it works because we instantiate a bean named conversionService
        // If we rename it, Spring won't find it, and it will fail
        return com.societegenerale.githubcrawler.FileToParseConversionService()
    }

    @Bean
    open fun crawler(remoteGitHub: RemoteGitHub,
                     ownershipParser: OwnershipParser,
                     output: List<GitHubCrawlerOutput>,
                     gitHubCrawlerProperties: GitHubCrawlerProperties,
                     environment : Environment): GitHubCrawler {

        val repositoryEnricher = RepositoryEnricher(remoteGitHub)

        return GitHubCrawler(remoteGitHub, ownershipParser, output, repositoryEnricher,gitHubCrawlerProperties,environment)
    }

    @Bean
    open fun remoteGitHub(@Value("\${gitHub.url}") gitHubUrl: String): RemoteGitHub {

        return RemoteGitHubImpl(gitHubUrl)
    }

    @Bean
    @ConditionalOnMissingBean(value = OwnershipParser::class)
    open fun dummyOwnershipParser(): OwnershipParser {

        return NoOpOwnershipParser()
    }

}
