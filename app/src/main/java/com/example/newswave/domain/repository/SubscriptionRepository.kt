package com.example.newswave.domain.repository

interface SubscriptionRepository {

    fun subscribeOnSource()

    fun unsubscribeOnSource()

}